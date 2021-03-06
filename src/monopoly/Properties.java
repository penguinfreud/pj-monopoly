package monopoly;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import monopoly.util.Consumer0;
import monopoly.util.Event3;
import monopoly.util.Util;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Properties {
    public interface IPlayerWithProperties extends IPlayer {
        default void askWhetherToBuyProperty(Consumer<Boolean> cb) {
            cb.accept(true);
        }

        default void askWhetherToUpgradeProperty(Consumer<Boolean> cb) {
            cb.accept(true);
        }

        default void askWhichPropertyToMortgage(Consumer<Property> cb) {
            cb.accept(get(this).getProperties().get(0));
        }
    }

    private static final Map<IPlayer, Properties> parasites = new Hashtable<>();
    public static final Map<Game, Event3<IPlayer, Boolean, Property>> onPropertyChange = new Hashtable<>();

    public static void enable(Game g) {
        if (onPropertyChange.get(g) == null) {
            onPropertyChange.put(g, new Event3<>());
            BasePlayer.onAddPlayer.get(g).addListener(player -> {
                Properties properties = new Properties(player);
                parasites.put(player, properties);
                player.addPossession(Util.sum(properties.properties, Property::mortgagePrice));
                player.addPropertySeller(properties::sellProperties);
            });
            BasePlayer.onBankrupt.get(g).addListener(player ->
                    parasites.get(player).properties.forEach(property -> property.resetOwner(g)));
        }
    }

    public static boolean isEnabled(Game g) {
        return onPropertyChange.get(g) != null;
    }

    public static Properties get(IPlayer player) {
        return parasites.get(player);
    }

    private final IPlayer player;
    private final Game game;
    private final ObservableList<Property> properties = FXCollections.observableList(new CopyOnWriteArrayList<>());
    private boolean rentFree = false;

    private Properties(IPlayer player) {
        this.player = player;
        game = player.getGame();

        if (!(player instanceof IPlayerWithProperties)) {
            game.triggerException("interface not implemented: IPlayerWithProperties");
        }

        game.onGameStart.addListener(() -> {
            properties.clear();
            rentFree = false;
        });
    }

    public final Game getGame() {
        return game;
    }

    public final IPlayer getPlayer() {
        return player;
    }

    public final ObservableList<Property> getProperties() {
        return properties;
    }

    public final int getPropertiesCount() {
        return properties.size();
    }

    private void sellProperties(Consumer0 cb) {
        sellProperties((Property) null, cb);
    }

    private void sellProperties(Property property, Consumer0 cb) {
        if (player.getCash() <= 0) {
            if (property != null) {
                if (properties.contains(property)) {
                    double amount = property.getMortgagePrice();
                    String msg = game.format("mortgage", player.getName(), property.getName(), amount);
                    properties.remove(property);
                    property.resetOwner(game);
                    player.changeCash(amount, msg);
                    onPropertyChange.get(game).trigger(player, false, property);
                } else {
                    game.triggerException("not_your_property");
                }
            }
            if (player.getCash() <= 0) {
                if (properties.size() > 0) {
                    ((IPlayerWithProperties) player)
                            .askWhichPropertyToMortgage(nextProp -> sellProperties(nextProp, cb));
                } else {
                    cb.accept();
                }
            } else {
                cb.accept();
            }
        } else {
            cb.accept();
        }
    }

    private boolean checkBuyingCondition(Property prop, boolean force) {
        if (player.getCash() >= prop.getPurchasePrice()) {
            if (prop.isFree()) {
                return true;
            } else if (force) {
                IPlayer owner = prop.getOwner();
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
        double price = property.getPurchasePrice();
        if (checkBuyingCondition(property, force)) {
            IPlayer owner = property.getOwner();
            String msg = game.format("buy_property", player.getName(), price, property.toString(game));
            player.pay(owner, price, msg, null);
            properties.add(property);
            if (owner != null) {
                get(owner).properties.remove(property);
            }
            property.ownerProperty().set(player);
            onPropertyChange.get(game).trigger(player, true, property);
        }
    }

    private void _upgradeProperty(Property property) {
        if (game.getState() == Game.State.TURN_LANDED) {
            double price = property.getUpgradePrice();
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
                cb.accept();
            } else {
                IPlayer owner = property.getOwner();
                double rent = property.getRent();
                String msg = game.format("pay_rent", player.getName(), owner.getName(), rent, property.toString(game));
                player.pay(owner, rent, msg, cb);
            }
        } else {
            Logger.getAnonymousLogger().log(Level.WARNING, Game.WRONG_STATE);
        }
    }

    public final void buyProperty(Property property, Consumer0 cb, boolean force) {
        synchronized (game.lock) {
            if (force) {
                _buyProperty(property, true);
                cb.accept();
            } else {
                double price = property.getPurchasePrice();
                if (property.isFree() && player.getCash() >= price) {
                    ((IPlayerWithProperties) player).askWhetherToBuyProperty((ok) -> {
                        synchronized (game.lock) {
                            if (ok) {
                                _buyProperty(property, false);
                            }
                            cb.accept();
                        }
                    });
                } else {
                    cb.accept();
                }
            }
        }
    }

    public final void buyProperty(Property property, Consumer0 cb) {
        synchronized (game.lock) {
            buyProperty(property, cb, false);
        }
    }

    final void upgradeProperty(Property property, Consumer0 cb) {
        if (game.getState() == Game.State.TURN_LANDED) {
            double price = property.getUpgradePrice();
            if (property.getOwner() == player && player.getCash() >= price) {
                ((IPlayerWithProperties) player).askWhetherToUpgradeProperty((ok) -> {
                    synchronized (game.lock) {
                        if (ok) {
                            _upgradeProperty(property);
                        }
                        cb.accept();
                    }
                });
            } else {
                cb.accept();
            }
        } else {
            Logger.getAnonymousLogger().log(Level.WARNING, Game.WRONG_STATE);
        }
    }

    public final void removeLand(Property property) {
        synchronized (game.lock) {
            if (property != null) {
                properties.remove(property);
                onPropertyChange.get(game).trigger(player, false, property);
            }
        }
    }

    public final void robLand(Property property) {
        synchronized (game.lock) {
            if (property != null) {
                IPlayer owner = property.getOwner();
                if (owner != player) {
                    property.ownerProperty().set(player);
                    properties.add(property);
                    if (owner != null) {
                        get(owner).properties.remove(property);
                        onPropertyChange.get(game).trigger(owner, false, property);
                    }
                    onPropertyChange.get(game).trigger(player, true, property);
                }
            }
        }
    }

    public final void setRentFree() {
        synchronized (game.lock) {
            rentFree = true;
        }
    }
}
