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

    private static final Parasite<Game, Event1<Triple<AbstractPlayer, Integer, String>>> _onMoneyChange = new Parasite<>(Game::onInit, Event1::New);
    private static final Parasite<Game, Event2<AbstractPlayer, Integer>> _onGetCoupons = new Parasite<>(Game::onInit, Event2::New);
    private static final Parasite<Game, Event2<AbstractPlayer, Card>> _onGetCard = new Parasite<>(Game::onInit, Event2::New);

    public static final EventWrapper<Game, Consumer1<Triple<AbstractPlayer, Integer, String>>> onMoneyChange = new EventWrapper<>(_onMoneyChange);
    public static final EventWrapper<Game, Consumer2<AbstractPlayer, Integer>> onGetCoupons = new EventWrapper<>(_onGetCoupons);
    public static final EventWrapper<Game, Consumer2<AbstractPlayer, Card>> onGetCard = new EventWrapper<>(_onGetCard);

    private String name;
    private Place currentPlace;
    private int cash, deposit, coupons;
    private boolean reversed = false;
    private boolean rentFree = false;
    private final List<Property> properties = new CopyOnWriteArrayList<>();
    private final List<Card> cards = new CopyOnWriteArrayList<>();
    private final Shareholding shareholding = new Shareholding();

    final void init(Game g) {
        if (g.getState() == Game.State.STARTING) {
            this.cash = g.getConfig("init-cash");
            this.deposit = g.getConfig("init-deposit");
            this.coupons = g.getConfig("init-coupons");
            currentPlace = g.getMap().getStartingPoint();
            properties.clear();
            cards.clear();
            reversed = false;
        } else {
            logger.log(Level.WARNING, Game.WRONG_STATE);
        }
    }

    public final String getName() {
        return name;
    }

    @Override
    public final String toString(Game g) {
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

    protected final void giveUp(Game g) {
        g.triggerBankrupt(this);
    }

    protected final void useCard(Game g, Card card, Consumer0 cb) {
        synchronized (g.lock) {
            if (g.getState() == Game.State.TURN_STARTING) {
                if (cards.contains(card)) {
                    cards.remove(card);
                    card.use(g, CardInterface.parasites.get(g), cb);
                } else {
                    g.triggerException("you_do_not_have_this_card");
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

    protected final void buyStock(Game g, Stock stock, int amount) {
        synchronized (g.lock) {
            if (g.getState() == Game.State.TURN_STARTING) {
                shareholding.buy(g, stock, amount);
            }
        }
    }

    protected final void sellStock(Game g, Stock stock, int amount) {
        synchronized (g.lock) {
            if (g.getState() == Game.State.TURN_STARTING) {
                shareholding.sell(g, stock, amount);
            }
        }
    }

    protected abstract void startTurn(Game g, Consumer0 cb);

    protected abstract void askWhetherToBuyProperty(Game g, Consumer1<Boolean> cb);

    protected abstract void askWhetherToUpgradeProperty(Game g, Consumer1<Boolean> cb);

    protected abstract void askWhichPropertyToMortgage(Game g, Consumer1<Property> cb);

    protected abstract void askWhichCardToBuy(Game g, Consumer1<Card> cb);

    protected abstract void askHowMuchToDepositOrWithdraw(Game g, Consumer1<Integer> cb);

    public abstract void askForPlayer(Game g, String reason, Consumer1<AbstractPlayer> cb);

    public abstract void askForPlace(Game g, String reason, Consumer1<Place> cb);

    final void changeCash(Game g, int amount, String msg) {
        if (cash + amount >= 0) {
            cash += amount;
            _onMoneyChange.get(g).trigger(new Triple<>(this, amount, msg));
        } else {
            g.triggerException("short_of_cash");
        }
    }

    final void changeDeposit(Game g, int amount, String msg) {
        if (deposit + amount >= 0) {
            deposit += amount;
            _onMoneyChange.get(g).trigger(new Triple<>(this, amount, msg));
        } else {
            g.triggerException("short_of_deposit");
        }
    }

    final void depositOrWithdraw(Game g, Consumer0 cb) {
        askHowMuchToDepositOrWithdraw(g, (amount) -> {
            int maxTransfer = g.getConfig("bank-max-transfer");
            if (-maxTransfer <= amount && amount <= maxTransfer &&
                    cash - amount >= 0 && deposit + amount >= 0) {
                cash -= amount;
                deposit += amount;
            }
            cb.run();
        });
    }

    private void sellProperties(Game g, Property prop, Consumer0 cb) {
        if (cash <= 0) {
            if (prop != null) {
                if (properties.contains(prop)) {
                    cash += prop.getMortgagePrice();
                    properties.remove(prop);
                    prop.resetOwner();
                } else {
                    g.triggerException("not_your_property");
                }
            }
            if (cash <= 0) {
                if (properties.size() > 0) {
                    askWhichPropertyToMortgage(g, (nextProp) -> sellProperties(g, nextProp, cb));
                } else {
                    g.triggerBankrupt(this);
                    cb.run();
                }
            } else {
                cb.run();
            }
        } else {
            cb.run();
        }
    }

    private boolean checkBuyingCondition(Game g, boolean force) {
        Property prop = currentPlace.asProperty();
        if (cash >= prop.getPurchasePrice()) {
            if (prop.isFree()) {
                return true;
            } else if (force) {
                AbstractPlayer owner = prop.getOwner();
                if (owner != this) {
                    return true;
                } else {
                    g.triggerException("you_cannot_buy_your_own_land");
                }
            } else {
                g.triggerException("cannot_buy_sold_land");
            }
        } else {
            g.triggerException("short_of_cash");
        }
        return false;
    }

    private void _buyProperty(Game g, boolean force) {
        Property prop = currentPlace.asProperty();
        int price = prop.getPurchasePrice();
        if (checkBuyingCondition(g, force)) {
            AbstractPlayer owner = prop.getOwner();
            pay(g, owner, price, "buy_property", null);
            properties.add(prop);
            if (owner != null) {
                owner.properties.remove(prop);
            }
            prop.changeOwner(this);
        }
    }

    private void _upgradeProperty(Game g) {
        if (g.getState() == Game.State.TURN_LANDED) {
            Property prop = currentPlace.asProperty();
            int price = prop.getUpgradePrice();
            if (prop.getOwner() == this) {
                if (cash >= price) {
                    changeCash(g, -price, "upgrade_property");
                    prop.upgrade(g);
                } else {
                    g.triggerException("short_of_cash");
                }
            } else {
                g.triggerException("not_your_property");
            }
        } else {
            logger.log(Level.WARNING, Game.WRONG_STATE);
        }
    }

    final void payRent(Game g, Consumer0 cb) {
        if (g.getState() == Game.State.TURN_LANDED) {
            if (rentFree) {
                rentFree = false;
                cb.run();
            } else {
                Property prop = currentPlace.asProperty();
                pay(g, prop.getOwner(), prop.getRent(), "pay_rent", cb);
            }
        } else {
            logger.log(Level.WARNING, Game.WRONG_STATE);
        }
    }

    final void buyProperty(Game g, Consumer0 cb, boolean force) {
        synchronized (g.lock) {
            Property prop = currentPlace.asProperty();
            int price = prop.getPurchasePrice();
            if (prop.isFree() && cash >= price) {
                askWhetherToBuyProperty(g, (ok) -> {
                    synchronized (g.lock) {
                        if (ok) {
                            _buyProperty(g, force);
                        }
                        cb.run();
                    }
                });
            } else {
                cb.run();
            }
        }
    }

    final void buyProperty(Game g, Consumer0 cb) {
        buyProperty(g, cb, false);
    }

    final void upgradeProperty(Game g, Consumer0 cb) {
        synchronized (g.lock) {
            if (g.getState() == Game.State.TURN_LANDED) {
                Property prop = currentPlace.asProperty();
                int price = prop.getUpgradePrice();
                if (prop.getOwner() == this && cash >= price) {
                    askWhetherToUpgradeProperty(g, (ok) -> {
                        synchronized (g.lock) {
                            if (ok) {
                                _upgradeProperty(g);
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
    }

    final void pay(Game g, AbstractPlayer receiver, int amount, String msg, Consumer0 cb) {
        cash -= amount;
        _onMoneyChange.get(g).trigger(new Triple<>(this, -amount, msg));
        if (receiver != null) {
            receiver.changeCash(g, Math.min(amount, getTotalPossessions() + amount), "get_" + msg);
        }
        if (cash < 0) {
            if (cash + deposit >= 0) {
                deposit += cash;
                cash = 0;
                cb.run();
            } else {
                cash += deposit;
                deposit = 0;
                sellProperties(g, null, cb);
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
        }
    }

    final void addCoupons(Game g, int amount) {
        coupons += amount;
        _onGetCoupons.get(g).trigger(this, amount);
    }

    final void addCard(Game g, Card card) {
        cards.add(card);
        _onGetCard.get(g).trigger(this, card);
    }

    final void removeCard(Game g, Card card) {
        if (cards.contains(card)) {
            cards.remove(card);
        }
    }

    final void buyCards(Game g, Consumer0 cb) {
        askWhichCardToBuy(g, new Consumer1<Card>() {
            @Override
            public void run(Card card) {
                synchronized (g.lock) {
                    if (card == null) {
                        cb.run();
                    } else {
                        int price = card.getPrice(g);
                        if (coupons >= price) {
                            cards.add(card);
                            coupons -= price;
                        }
                        askWhichCardToBuy(g, this);
                    }
                }
            }
        });
    }

    private int stepsToAdvance;

    final void startWalking(Game g, int steps) {
        if (g.getState() == Game.State.TURN_WALKING) {
            stepsToAdvance = steps;
            startStep(g);
        } else {
            logger.log(Level.WARNING, Game.WRONG_STATE);
        }
    }

    protected void startStep(Game g) {
        endStep(g);
    }

    protected final void endStep(Game g) {
        synchronized (g.lock) {
            if (g.getState() == Game.State.TURN_WALKING) {
                currentPlace = reversed? currentPlace.getPrev(): currentPlace.getNext();
                --stepsToAdvance;
                if (currentPlace.hasRoadblock()) {
                    stepsToAdvance = 0;
                    currentPlace.clearRoadblocks();
                    g.triggerException("met_roadblock", currentPlace.toString(g));
                }
                if (stepsToAdvance == 0) {
                    g.endWalking();
                } else {
                    currentPlace.onPassingBy(g, PlaceInterface.parasites.get(g), () -> startStep(g));
                }
            } else {
                logger.log(Level.WARNING, Game.WRONG_STATE);
            }
        }
    }

    final void onLanded(Game g, Consumer0 cb) {
        currentPlace.onLanded(g, PlaceInterface.parasites.get(g), cb);
    }
}
