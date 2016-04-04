package monopoly;

import monopoly.card.Card;
import monopoly.stock.Stock;
import monopoly.util.*;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractPlayer implements Serializable, GameObject {
    static {
        Game.putDefaultConfig("init-cash", 2000);
        Game.putDefaultConfig("init-deposit", 2000);
        Game.putDefaultConfig("init-coupons", 0);
        Game.putDefaultConfig("bank-max-transfer", 100000);
    }

    private static final Logger logger = Logger.getLogger(AbstractPlayer.class.getName());

    private static final Parasite<Game, Event3<AbstractPlayer, Integer, String>> _onMoneyChange = new Parasite<>(Game::onInit, Event3::New);
    private static final Parasite<Game, Event3<AbstractPlayer, Boolean, Property>> _onPropertyChange = new Parasite<>(Game::onInit, Event3::New);
    private static final Parasite<Game, Event2<AbstractPlayer, Integer>> _onCouponChange = new Parasite<>(Game::onInit, Event2::New);
    private static final Parasite<Game, Event3<AbstractPlayer, Boolean, Card>> _onCardChange = new Parasite<>(Game::onInit, Event3::New);
    private static final Parasite<Game, Event3<AbstractPlayer, Stock, Integer>> _onStockHoldingChange = new Parasite<>(Game::onInit, Event3::New);

    public static final EventWrapper<Game, Consumer3<AbstractPlayer, Integer, String>> onMoneyChange = new EventWrapper<>(_onMoneyChange);
    public static final EventWrapper<Game, Consumer3<AbstractPlayer, Boolean, Property>> onPropertyChange = new EventWrapper<>(_onPropertyChange);
    public static final EventWrapper<Game, Consumer2<AbstractPlayer, Integer>> onCouponChange = new EventWrapper<>(_onCouponChange);
    public static final EventWrapper<Game, Consumer3<AbstractPlayer, Boolean, Card>> onCardChange = new EventWrapper<>(_onCardChange);
    public static final EventWrapper<Game, Consumer3<AbstractPlayer, Stock, Integer>> onStockHoldingChange = new EventWrapper<>(_onStockHoldingChange);

    private Game game;
    private String name;
    private Place currentPlace;
    private int cash, deposit, coupons;
    private boolean reversed = false;
    private boolean rentFree = false;
    private final List<Property> properties = new CopyOnWriteArrayList<>();
    private final List<Card> cards = new CopyOnWriteArrayList<>();
    private final Shareholding shareholding = new Shareholding();
    
    final void setGame(Game game) {
        this.game = game;
    }

    final void init() {
        if (game.getState() == Game.State.STARTING) {
            this.cash = game.getConfig("init-cash");
            this.deposit = game.getConfig("init-deposit");
            this.coupons = game.getConfig("init-coupons");
            currentPlace = game.getMap().getStartingPoint();
            properties.clear();
            cards.clear();
            reversed = false;
        } else {
            logger.log(Level.WARNING, Game.WRONG_STATE);
        }
    }

    public final Game getGame() {
        return game;
    }

    public final String getName() {
        return name;
    }

    @Override
    public final String toString(Game game) {
        return name;
    }

    protected final void setName(String name) {
        this.name = name;
    }

    public final int getCash() {
        return cash;
    }

    public final int getDeposit() {
        return deposit;
    }

    public final int getCoupons() {
        return coupons;
    }

    public final Place getCurrentPlace() {
        return currentPlace;
    }

    public final List<Property> getProperties() {
        return new CopyOnWriteArrayList<>(properties);
    }

    public final List<Card> getCards() {
        return new CopyOnWriteArrayList<>(cards);
    }

    public final int getTotalPossessions() {
        int poss = cash + deposit;
        for (Property prop : properties) {
            poss += prop.getMortgagePrice();
        }
        return poss;
    }

    public final boolean isReversed() {
        return reversed;
    }

    final void reverse() {
        reversed = !reversed;
    }

    final void setRentFree() {
        rentFree = true;
    }

    protected final void giveUp() {
        game.triggerBankrupt(this);
    }

    protected final void useCard(Card card, Consumer0 cb) {
        synchronized (game.lock) {
            if (game.getState() == Game.State.TURN_STARTING) {
                if (cards.contains(card)) {
                    cards.remove(card);
                    _onCardChange.get(game).trigger(this, false, card);
                    card.use(game, CardInterface.parasites.get(game), cb);
                } else {
                    game.triggerException("you_do_not_have_this_card");
                    cb.run();
                }
            } else {
                logger.log(Level.WARNING, Game.WRONG_STATE);
            }
        }
    }

    public final Shareholding.StockHolding getHolding(Stock stock) {
        return shareholding.getHolding(stock);
    }

    protected final void buyStock(Stock stock, int amount) {
        synchronized (game.lock) {
            if (game.getState() == Game.State.TURN_STARTING) {
                shareholding.buy(game, stock, amount);
                _onStockHoldingChange.get(game).trigger(this, stock, amount);
            }
        }
    }

    protected final void sellStock(Stock stock, int amount) {
        synchronized (game.lock) {
            if (game.getState() == Game.State.TURN_STARTING) {
                shareholding.sell(game, stock, amount);
                _onStockHoldingChange.get(game).trigger(this, stock, -amount);
            }
        }
    }

    protected abstract void startTurn(Consumer0 cb);

    protected abstract void askWhetherToBuyProperty(Consumer1<Boolean> cb);

    protected abstract void askWhetherToUpgradeProperty(Consumer1<Boolean> cb);

    protected abstract void askWhichPropertyToMortgage(Consumer1<Property> cb);

    protected abstract void askWhichCardToBuy(Consumer1<Card> cb);

    protected abstract void askHowMuchToDepositOrWithdraw(Consumer1<Integer> cb);

    public abstract void askForPlayer(String reason, Consumer1<AbstractPlayer> cb);

    public abstract void askForPlace(String reason, Consumer1<Place> cb);

    final void changeCash(int amount, String msg) {
        if (cash + amount >= 0) {
            cash += amount;
            _onMoneyChange.get(game).trigger(this, amount, msg);
        } else {
            game.triggerException("short_of_cash");
        }
    }

    final void changeDeposit(int amount, String msg) {
        if (deposit + amount >= 0) {
            deposit += amount;
            _onMoneyChange.get(game).trigger(this, amount, msg);
        } else {
            game.triggerException("short_of_deposit");
        }
    }

    final void depositOrWithdraw(Consumer0 cb) {
        askHowMuchToDepositOrWithdraw((amount) -> {
            int maxTransfer = game.getConfig("bank-max-transfer");
            if (-maxTransfer <= amount && amount <= maxTransfer &&
                    cash - amount >= 0 && deposit + amount >= 0) {
                cash -= amount;
                deposit += amount;
            }
            cb.run();
        });
    }

    private void sellProperties(Property prop, Consumer0 cb) {
        if (cash <= 0) {
            if (prop != null) {
                if (properties.contains(prop)) {
                    cash += prop.getMortgagePrice();
                    properties.remove(prop);
                    prop.resetOwner();
                    _onPropertyChange.get(game).trigger(this, false, prop);
                } else {
                    game.triggerException("not_your_property");
                }
            }
            if (cash <= 0) {
                if (properties.size() > 0) {
                    askWhichPropertyToMortgage((nextProp) -> sellProperties(nextProp, cb));
                } else {
                    game.triggerBankrupt(this);
                    cb.run();
                }
            } else {
                cb.run();
            }
        } else {
            cb.run();
        }
    }

    private boolean checkBuyingCondition(boolean force) {
        Property prop = currentPlace.asProperty();
        if (cash >= prop.getPurchasePrice()) {
            if (prop.isFree()) {
                return true;
            } else if (force) {
                AbstractPlayer owner = prop.getOwner();
                if (owner != this) {
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

    private void _buyProperty(boolean force) {
        Property prop = currentPlace.asProperty();
        int price = prop.getPurchasePrice();
        if (checkBuyingCondition(force)) {
            AbstractPlayer owner = prop.getOwner();
            String msg = game.format("buy_property", getName(), price, prop.toString(game));
            pay(owner, price, msg, null);
            properties.add(prop);
            if (owner != null) {
                owner.properties.remove(prop);
            }
            prop.changeOwner(this);
            _onPropertyChange.get(game).trigger(this, true, prop);
        }
    }

    private void _upgradeProperty() {
        if (game.getState() == Game.State.TURN_LANDED) {
            Property prop = currentPlace.asProperty();
            int price = prop.getUpgradePrice();
            if (prop.getOwner() == this) {
                if (cash >= price) {
                    String msg = game.format("upgrade_property", name, price, prop.toString(game), prop.getLevel() + 1);
                    changeCash(-price, msg);
                    prop.upgrade(game);
                } else {
                    game.triggerException("short_of_cash");
                }
            } else {
                game.triggerException("not_your_property");
            }
        } else {
            logger.log(Level.WARNING, Game.WRONG_STATE);
        }
    }

    final void payRent(Consumer0 cb) {
        if (game.getState() == Game.State.TURN_LANDED) {
            if (rentFree) {
                rentFree = false;
                cb.run();
            } else {
                Property prop = currentPlace.asProperty();
                AbstractPlayer owner = prop.getOwner();
                int rent = prop.getRent();
                String msg = game.format("pay_rent", name, owner.name, rent, prop.toString(game));
                pay(owner, rent, msg, cb);
            }
        } else {
            logger.log(Level.WARNING, Game.WRONG_STATE);
        }
    }

    final void buyProperty(Consumer0 cb, boolean force) {
        synchronized (game.lock) {
            Property prop = currentPlace.asProperty();
            int price = prop.getPurchasePrice();
            if (prop.isFree() && cash >= price) {
                askWhetherToBuyProperty((ok) -> {
                    synchronized (game.lock) {
                        if (ok) {
                            _buyProperty(force);
                        }
                        cb.run();
                    }
                });
            } else {
                cb.run();
            }
        }
    }

    final void buyProperty(Consumer0 cb) {
        buyProperty(cb, false);
    }

    final void upgradeProperty(Consumer0 cb) {
        if (game.getState() == Game.State.TURN_LANDED) {
            Property prop = currentPlace.asProperty();
            int price = prop.getUpgradePrice();
            if (prop.getOwner() == this && cash >= price) {
                askWhetherToUpgradeProperty((ok) -> {
                    synchronized (game.lock) {
                        if (ok) {
                            _upgradeProperty();
                        }
                        cb.run();
                    }
                });
            } else {
                cb.run();
            }
        } else {
            logger.log(Level.WARNING, Game.WRONG_STATE);
        }
    }

    final void pay(AbstractPlayer receiver, int amount, String msg, Consumer0 cb) {
        cash -= amount;
        _onMoneyChange.get(game).trigger(this, -amount, msg);
        if (receiver != null) {
            receiver.changeCash(Math.min(amount, getTotalPossessions() + amount), "");
        }
        if (cash < 0) {
            if (cash + deposit >= 0) {
                deposit += cash;
                cash = 0;
                cb.run();
            } else {
                cash += deposit;
                deposit = 0;
                sellProperties(null, cb);
            }
        } else if (cb != null) {
            cb.run();
        }
    }

    final void robLand() {
        Property prop = currentPlace.asProperty();
        if (prop != null) {
            AbstractPlayer owner = prop.getOwner();
            if (owner != null) {
                owner.properties.remove(prop);
            }
            prop.changeOwner(this);
            _onPropertyChange.get(game).trigger(this, true, prop);
        }
    }

    final void addCoupons(int amount) {
        coupons += amount;
        _onCouponChange.get(game).trigger(this, amount);
    }

    final void addCard(Card card) {
        cards.add(card);
        _onCardChange.get(game).trigger(this, true, card);
    }

    final void removeCard(Card card) {
        if (cards.contains(card)) {
            cards.remove(card);
            _onCardChange.get(game).trigger(this, false, card);
        }
    }

    final void buyCards(Consumer0 cb) {
        askWhichCardToBuy(new Consumer1<Card>() {
            @Override
            public void run(Card card) {
                synchronized (game.lock) {
                    if (card == null) {
                        cb.run();
                    } else {
                        int price = card.getPrice(game);
                        if (coupons >= price) {
                            cards.add(card);
                            coupons -= price;
                        }
                        askWhichCardToBuy(this);
                    }
                }
            }
        });
    }

    private int stepsToAdvance;

    final void startWalking(int steps) {
        if (game.getState() == Game.State.TURN_WALKING) {
            stepsToAdvance = steps;
            startStep();
        } else {
            logger.log(Level.WARNING, Game.WRONG_STATE);
        }
    }

    protected void startStep() {
        endStep();
    }

    protected final void endStep() {
        synchronized (game.lock) {
            if (game.getState() == Game.State.TURN_WALKING) {
                currentPlace = reversed? currentPlace.getPrev(): currentPlace.getNext();
                --stepsToAdvance;
                if (currentPlace.hasRoadblock()) {
                    stepsToAdvance = 0;
                    currentPlace.clearRoadblocks();
                    game.triggerException("met_roadblock", currentPlace.toString(game));
                }
                if (stepsToAdvance == 0) {
                    game.endWalking();
                } else {
                    currentPlace.onPassingBy(game, PlaceInterface.parasites.get(game), this::startStep);
                }
            } else {
                logger.log(Level.WARNING, Game.WRONG_STATE);
            }
        }
    }

    final void onLanded(Consumer0 cb) {
        currentPlace.onLanded(game, PlaceInterface.parasites.get(game), cb);
    }
}
