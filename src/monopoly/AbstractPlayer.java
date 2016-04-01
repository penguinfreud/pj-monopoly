package monopoly;

import jdk.nashorn.internal.codegen.CompilerConstants;
import monopoly.async.Callback;
import monopoly.async.MoneyChangeEvent;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractPlayer implements Serializable {
    private String name;
    private Place currentPlace;
    private int cash, deposit, coupons;
    private boolean reversed = false;
    private List<Property> properties = new CopyOnWriteArrayList<>();
    private List<Card> cards = new CopyOnWriteArrayList<>();

    final void init(int cash, int deposit, int coupons, Place place) {
        this.cash = cash;
        this.deposit = deposit;
        this.coupons = coupons;
        currentPlace = place;
        properties.clear();
        cards.clear();
        reversed = false;
    }

    public final String getName() {
        return name;
    }

    @Override
    public String toString() {
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

    public Place getCurrentPlace() {
        return currentPlace;
    }

    public final List<Property> getProperties() {
        return new CopyOnWriteArrayList<>(properties);
    }

    public final List<Card> getCards() {
        return new CopyOnWriteArrayList<>(cards);
    }

    public int getTotalPossessions() {
        int poss = cash + deposit;
        for (Property prop: properties) {
            poss += prop.getMortgagePrice();
        }
        return poss;
    }

    public boolean isReversed() {
        return reversed;
    }

    protected final void giveUp(Game g) {
        g.triggerBankrupt(this);
    }

    private Callback<Object> useCardCb;
    private Callback<Card> selectCardCb;

    final void startTurn(Game g) {
        selectCardCb = (card) -> {
            synchronized (g.lock) {
                if (g.getState() == Game.State.TURN_STARTING) {
                    if (card == null) {
                        g.rollTheDice();
                    } else if (cards.contains(card)) {
                        cards.remove(card);
                        g.useCard(card, useCardCb);
                    }
                }
            }
        };
        useCardCb = (o) -> {
            synchronized (g.lock) {
                if (g.getState() == Game.State.TURN_STARTING) {
                    askWhichCardToUse(g, selectCardCb);
                }
            }
        };
        useCardCb.run(null);
    }

    protected abstract void askWhetherToBuyProperty(Game g, Callback<Boolean> cb);
    protected abstract void askWhetherToUpgradeProperty(Game g, Callback<Boolean> cb);
    protected abstract void askWhichPropertyToMortgage(Game g, Callback<Property> cb);
    protected abstract void askWhichCardToBuy(Game g, Callback<Card> cb);
    protected abstract void askWhichCardToUse(Game g, Callback<Card> cb);
    protected abstract void askHowMuchToDepositOrWithdraw(Game g, Callback<Integer> cb);

    public abstract void askWhomToReverse(Game g, Callback<AbstractPlayer> cb);
    public abstract void askWhereToGo(Game g, Callback<Place> cb);
    public abstract void askWhereToSetRoadblock(Game g, Callback<Place> cb);

    private void changeCash(Game g, int amount, String msg) {
        if (cash + amount >= 0) {
            cash += amount;
            g.triggerMoneyChange(new MoneyChangeEvent(this, amount, msg));
        }
    }

    final void changeDeposit(Game g, int amount, String msg) {
        synchronized (g.lock) {
            if (deposit + amount >= 0) {
                deposit += amount;
                g.triggerMoneyChange(new MoneyChangeEvent(this, amount, msg));
            }
        }
    }

    private void sellProperties(Game g, Property prop, Callback<Object> cb) {
        if (cash <= 0) {
            if (prop != null) {
                if (properties.contains(prop)) {
                    cash += prop.getMortgagePrice();
                    properties.remove(prop);
                    prop.mortgage();
                }
            }
            if (cash <= 0) {
                if (properties.size() > 0) {
                    askWhichPropertyToMortgage(g, (nextProp) -> sellProperties(g, nextProp, cb));
                } else {
                    g.triggerBankrupt(this);
                    cb.run(null);
                }
            } else {
                cb.run(null);
            }
        } else {
            cb.run(null);
        }
    }

    private void _buyProperty(Game g) {
        if (g.getState() == Game.State.TURN_LANDED) {
            Property prop = currentPlace.asProperty();
            int price = prop.getPurchasePrice();
            if (prop.isFree() && cash >= price) {
                changeCash(g, -price, "buy_property");
                properties.add(prop);
                prop.changeOwner(AbstractPlayer.this);
            }
        }
    }

    private void _upgradeProperty(Game g) {
        if (g.getState() == Game.State.TURN_LANDED) {
            Property prop = currentPlace.asProperty();
            int price = prop.getUpgradePrice();
            if (prop.getOwner() == this && cash >= price) {
                changeCash(g, -price, "upgrade_property");
                prop.upgrade(g);
            }
        }
    }

    final void payRent(Game g, Callback<Object> cb) {
        synchronized (g.lock) {
            if (g.getState() == Game.State.TURN_LANDED) {
                System.out.println("pay rent");
                Property prop = currentPlace.asProperty();
                pay(g, prop.getOwner(), prop.getRent(), "pay_rent", cb);
            }
        }
    }

    final void buyProperty(Game g, Callback<Object> cb) {
        synchronized (g.lock) {
            if (g.getState() == Game.State.TURN_LANDED) {
                Property prop = currentPlace.asProperty();
                int price = prop.getPurchasePrice();
                if (prop.isFree() && cash >= price) {
                    askWhetherToBuyProperty(g, (ok) -> {
                        synchronized (g.lock) {
                            _buyProperty(g);
                            cb.run(null);
                        }
                    });
                } else {
                    cb.run(null);
                }
            }
        }
    }

    final void upgradeProperty(Game g, Callback<Object> cb) {
        synchronized (g.lock) {
            if (g.getState() == Game.State.TURN_LANDED) {
                Property prop = currentPlace.asProperty();
                int price = prop.getUpgradePrice();
                if (prop.getOwner() == this && cash >= price) {
                    askWhetherToUpgradeProperty(g, (ok) -> {
                        synchronized (g.lock) {
                            _upgradeProperty(g);
                            cb.run(null);
                        }
                    });
                } else {
                    cb.run(null);
                }
            }
        }
    }

    final void pay(Game g, AbstractPlayer receiver, int amount, String msg, Callback<Object> cb) {
        synchronized (g.lock) {
            cash -= amount;
            g.triggerMoneyChange(new MoneyChangeEvent(this, -amount, msg));
            if (receiver != null) {
                receiver.changeCash(g, Math.min(amount, getTotalPossessions()), "get_" + msg);
            }
            if (cash <= 0) {
                if (cash + deposit >= 0) {
                    deposit += cash;
                    cash = 0;
                    cb.run(null);
                } else {
                    cash += deposit;
                    deposit = 0;
                    sellProperties(g, null, cb);
                }
            } else {
                cb.run(null);
            }
        }
    }

    private int stepsToAdvance;

    final void startWalking(Game g, int steps) {
        synchronized (g.lock) {
            if (g.getState() == Game.State.TURN_WALKING) {
                stepsToAdvance = steps;
                startStep(g);
            }
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
                }
                if (stepsToAdvance == 0) {
                    g.endWalking();
                } else {
                    g.passBy(currentPlace, (o) -> startStep(g));
                }
            }
        }
    }

    public static final class PlaceInterface implements Serializable {
        public final void changeCash(AbstractPlayer player, Game g, int amount, String msg) {
            synchronized (g.lock) {
                player.changeCash(g, amount, msg);
            }
        }

        public final void changeDeposit(AbstractPlayer player, Game g, int amount, String msg) {
            player.changeDeposit(g, amount, msg);
        }

        public final void depositOrWithdraw(Game g, Callback<Object> cb) {
            synchronized (g.lock) {
                AbstractPlayer player = g.getCurrentPlayer();
                player.askHowMuchToDepositOrWithdraw(g, (amount) -> {
                    int maxTransfer = (Integer) g.getConfig("bank-max-transfer");
                    if (-maxTransfer <= amount && amount <= maxTransfer &&
                            player.cash - amount >= 0 && player.deposit + amount >= 0) {
                        player.cash -= amount;
                        player.deposit += amount;
                    }
                    cb.run(null);
                });
            }
        }

        public final void pay(AbstractPlayer player, Game g, AbstractPlayer receiver, int amount, String msg, Callback<Object> cb) {
            player.pay(g, receiver, amount, msg, cb);
        }

        public final void addCoupons(AbstractPlayer player, Game g, int amount) {
            synchronized (g.lock) {
                player.coupons += amount;
            }
        }

        public final void addCard(AbstractPlayer player, Game g, Card card) {
            player.cards.add(card);
        }

        private Callback<Card> buyCardCb;

        public final void buyCards(Game g, Callback<Object> cb) {
            synchronized (g.lock) {
                AbstractPlayer player = g.getCurrentPlayer();
                buyCardCb = (card) -> {
                    synchronized (g.lock) {
                        if (card == null) {
                            cb.run(null);
                        } else {
                            int price = card.getPrice(g);
                            if (player.coupons >= price) {
                                player.cards.add(card);
                                player.coupons -= price;
                            }
                            player.askWhichCardToBuy(g, buyCardCb);
                        }
                    }
                };
                player.askWhichCardToBuy(g, buyCardCb);
            }
        }
    }

    public static final class CardInterface implements Serializable {
        public final void reverse(AbstractPlayer player) {
            player.reversed = !player.reversed;
        }

        public final void walk(Game g, int steps) {
            if (steps > 0 && steps <= (Integer) g.getConfig("dice-sides")) {
                g.startWalking(steps);
            }
        }

        public final void stay(Game g) {
            g.stay();
        }

        public final void setRoadblock(Place place) {
            place.setRoadblock();
        }
    }
}
