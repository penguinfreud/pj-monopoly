package monopoly;

import monopoly.util.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Properties {
    public interface IPlayerWithProperties {
        void askWhetherToBuyProperty(Consumer1<Boolean> cb);
        void askWhetherToUpgradeProperty(Consumer1<Boolean> cb);
        void askWhichPropertyToMortgage(Consumer1<Property> cb);
    }

    private static final Parasite<AbstractPlayer, Properties> parasites = new Parasite<>(AbstractPlayer::onInit, Properties::new);
    private static final Parasite<AbstractPlayer, Event2<Boolean, Property>> _onPropertyChange = new Parasite<>(AbstractPlayer::onInit, Event2::New);
    public static final EventWrapper<AbstractPlayer, Consumer2<Boolean, Property>> onPropertyChange = new EventWrapper<>(_onPropertyChange);

    static {
        AbstractPlayer.addPossession(player -> parasites.get(player).getValue());
        AbstractPlayer.addPropertySeller(Properties::sellProperties);
    }

    public static Properties get(AbstractPlayer player) {
        return parasites.get(player);
    }

    private final AbstractPlayer player;
    private final Game game;
    private final List<Property> properties = new CopyOnWriteArrayList<>();
    private boolean rentFree = false;

    protected Properties(AbstractPlayer player) {
        this.player = player;
        game = player.getGame();
    }

    final void init() {
        properties.clear();
        rentFree = false;
    }

    final void setRentFree() {
        rentFree = true;
    }

    public final int getValue() {
        return properties.stream().map(Property::getMortgagePrice).reduce(0, (a, b) -> a + b);
    }

    public final List<Property> getProperties() {
        return new CopyOnWriteArrayList<>(properties);
    }

    public final int getPropertiesCount() {
        return properties.size();
    }

    private static void sellProperties(AbstractPlayer player, Consumer0 cb) {
        parasites.get(player).sellProperties((Property)null, cb);
    }

    private void sellProperties(Property property, Consumer0 cb) {
        if (player.getCash() <= 0) {
            if (property != null) {
                if (properties.contains(property)) {
                    int amount = property.getMortgagePrice();
                    String msg = game.format("mortgage", player.getName(), property.getName(), amount);
                    properties.remove(property);
                    property.resetOwner();
                    player.changeCash(amount, msg);
                    _onPropertyChange.get(player).trigger(false, property);
                } else {
                    game.triggerException("not_your_property");
                }
            }
            if (player.getCash() <= 0) {
                if (properties.size() > 0) {
                    ((IPlayerWithProperties) player)
                            .askWhichPropertyToMortgage(nextProp -> sellProperties(nextProp, cb));
                } else {
                    game.triggerBankrupt(player);
                    cb.run();
                }
            } else {
                cb.run();
            }
        } else {
            cb.run();
        }
    }

    private boolean checkBuyingCondition(Property prop, boolean force) {
        if (player.getCash() >= prop.getPurchasePrice()) {
            if (prop.isFree()) {
                return true;
            } else if (force) {
                AbstractPlayer owner = prop.getOwner();
                if (owner != player) {
                    return true;
                } else {
                    game.triggerException("you_cannot_buy_your_own_land");
                }
            } else {
                game.triggerException("cannot_buy_sold_land");
            }
        } else {
            game.triggerException("short_of_cash");
        }
        return false;
    }

    private void _buyProperty(Property property, boolean force) {
        int price = property.getPurchasePrice();
        if (checkBuyingCondition(property, force)) {
            AbstractPlayer owner = property.getOwner();
            String msg = game.format("buy_property", player.getName(), price, property.toString(game));
            player.pay(owner, price, msg, null);
            properties.add(property);
            if (owner != null) {
                get(owner).properties.remove(property);
            }
            property.changeOwner(player);
            _onPropertyChange.get(player).trigger(true, property);
        }
    }


    private void _upgradeProperty(Property property) {
        if (game.getState() == Game.State.TURN_LANDED) {
            int price = property.getUpgradePrice();
            if (property.getOwner() == player) {
                if (player.getCash() >= price) {
                    String msg = game.format("upgrade_property", player.getName(), price, property.toString(game), property.getLevel() + 1);
                    player.changeCash(-price, msg);
                    property.upgrade(game);
                } else {
                    game.triggerException("short_of_cash");
                }
            } else {
                game.triggerException("not_your_property");
            }
        } else {
            Logger.getAnonymousLogger().log(Level.WARNING, Game.WRONG_STATE);
        }
    }

    final void payRent(Property property, Consumer0 cb) {
        if (game.getState() == Game.State.TURN_LANDED) {
            if (rentFree) {
                rentFree = false;
                cb.run();
            } else {
                AbstractPlayer owner = property.getOwner();
                int rent = property.getRent();
                String msg = game.format("pay_rent", player.getName(), owner.getName(), rent, property.toString(game));
                player.pay(owner, rent, msg, cb);
            }
        } else {
            Logger.getAnonymousLogger().log(Level.WARNING, Game.WRONG_STATE);
        }
    }

    final void buyProperty(Property property, Consumer0 cb, boolean force) {
        synchronized (game.lock) {
            int price = property.getPurchasePrice();
            if (property.isFree() && player.getCash() >= price) {
                ((IPlayerWithProperties) player).askWhetherToBuyProperty((ok) -> {
                    synchronized (game.lock) {
                        if (ok) {
                            _buyProperty(property, force);
                        }
                        cb.run();
                    }
                });
            } else {
                cb.run();
            }
        }
    }

    final void buyProperty(Property property, Consumer0 cb) {
        buyProperty(property, cb, false);
    }

    final void upgradeProperty(Property property, Consumer0 cb) {
        if (game.getState() == Game.State.TURN_LANDED) {
            int price = property.getUpgradePrice();
            if (property.getOwner() == player && player.getCash() >= price) {
                ((IPlayerWithProperties) player).askWhetherToUpgradeProperty((ok) -> {
                    synchronized (game.lock) {
                        if (ok) {
                            _upgradeProperty(property);
                        }
                        cb.run();
                    }
                });
            } else {
                cb.run();
            }
        } else {
            Logger.getAnonymousLogger().log(Level.WARNING, Game.WRONG_STATE);
        }
    }

    final void robLand(Property property) {
        if (property != null) {
            AbstractPlayer owner = property.getOwner();
            if (owner != null) {
                properties.remove(property);
            }
            property.changeOwner(player);
            if (owner != null) {
                _onPropertyChange.get(owner).trigger(false, property);
            }
            _onPropertyChange.get(player).trigger(true, property);
        }
    }
}
