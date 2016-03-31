package monopoly;

import monopoly.async.Callback;
import monopoly.async.MoneyChangeEvent;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractPlayer implements Serializable {
    private String name;
    private Place currentPlace;
    private int cash, deposit;
    private boolean reversed = false;
    private List<Property> properties = new CopyOnWriteArrayList<>();
    private List<Card> cards = new CopyOnWriteArrayList<>();

    final void initPlace(Place place) {
        currentPlace = place;
    }

    final void initCash(int cash) {
        this.cash = cash;
    }

    final void initDeposit(int deposit) {
        this.deposit = deposit;
    }

    final void initProperties() {
        properties.clear();
    }

    final void initCards() {
        cards.clear();
    }

    public final String getName() {
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

    public Place getCurrentPlace() {
        return currentPlace;
    }

    public final List<Property> getProperties() {
        return new CopyOnWriteArrayList<>(properties);
    }

    public int getTotalPossessions() {
        int poss = cash + deposit;
        for (Property prop: properties) {
            poss += prop.getMortgagePrice();
        }
        return poss;
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
                    } else {
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
    protected abstract void askWhichCardToUse(Game g, Callback<Card> cb);
    protected abstract void askHowMuchToDepositOrWithdraw(Game g, Callback<Integer> cb);

    private void changeCash(Game g, int amount, String msg) {
        synchronized (g.lock) {
            if (cash + amount >= 0) {
                cash += amount;
                g.triggerMoneyChange(new MoneyChangeEvent(this, amount, msg));
            }
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
        if (cash < 0) {
            if (prop != null) {
                if (properties.contains(prop)) {
                    cash += prop.getMortgagePrice();
                    properties.remove(prop);
                    prop.mortgage();
                }
            }
            if (cash < 0) {
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
        synchronized (g.lock) {
            if (g.getState() == Game.State.TURN_LANDED) {
                Property prop = currentPlace.asProperty();
                int price = prop.getPurchasePrice();
                if (prop.isFree() && cash > price) {
                    changeCash(g, -price, "");
                    properties.add(prop);
                    prop.changeOwner(AbstractPlayer.this);
                }
            }
        }
    }

    private void _upgradeProperty(Game g) {
        synchronized (g.lock) {
            if (g.getState() == Game.State.TURN_LANDED) {
                Property prop = currentPlace.asProperty();
                int price = prop.getUpgradePrice();
                if (prop.isFree() && cash > price) {
                    changeCash(g, -price, "");
                    prop.upgrade(g);
                }
            }
        }
    }

    final void payRent(Game g, Callback<Object> cb) {
        synchronized (g.lock) {
            if (g.getState() == Game.State.TURN_LANDED) {
                System.out.println("pay rent");
                pay(g, currentPlace.asProperty().getRent(), "", cb);
            }
        }
    }

    final void buyProperty(Game g, Callback<Object> cb) {
        synchronized (g.lock) {
            if (g.getState() == Game.State.TURN_LANDED) {
                Property prop = currentPlace.asProperty();
                int price = prop.getPurchasePrice();
                if (prop.isFree() && cash > price) {
                    askWhetherToBuyProperty(g, (ok) -> {
                        _buyProperty(g);
                        cb.run(null);
                    });
                }
            }
        }
    }

    final void upgradeProperty(Game g, Callback<Object> cb) {
        synchronized (g.lock) {
            if (g.getState() == Game.State.TURN_LANDED) {
                Property prop = currentPlace.asProperty();
                int price = prop.getUpgradePrice();
                if (prop.isFree() && cash > price) {
                    askWhetherToUpgradeProperty(g, (ok) -> {
                        _upgradeProperty(g);
                        cb.run(null);
                    });
                }
            }
        }
    }

    final void pay(Game g, int amount, String msg, Callback<Object> cb) {
        synchronized (g.lock) {
            cash -= amount;
            g.triggerMoneyChange(new MoneyChangeEvent(this, -amount, msg));
            if (cash < 0) {
                if (cash + deposit >= 0) {
                    cash = 0;
                    deposit += cash;
                } else {
                    cash += deposit;
                    deposit = 0;
                    sellProperties(g, null, cb);
                    if (cash <= 0) {
                        g.triggerBankrupt(AbstractPlayer.this);
                        cb.run(null);
                    }
                }
            }
        }
    }

    private int stepsToAdvance;

    final void startWalking(Game g, int steps) {
        synchronized (g.lock) {
            if (g.getState() == Game.State.TURN_WALKING) {
                System.out.println("start walking");
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
                System.out.println("endStep " + getCurrentPlace().getName());
                currentPlace = reversed? currentPlace.getPrev(): currentPlace.getNext();
                if (--stepsToAdvance == 0) {
                    g.endWalking();
                } else {
                    g.passBy(currentPlace, (o) -> startStep(g));
                }
            }
        }
    }

    public static final class PlaceInterface implements Serializable {
        public final void changeCash(AbstractPlayer player, Game g, int amount, String msg) {
            player.changeCash(g, amount, msg);
        }

        public final void changeDeposit(AbstractPlayer player, Game g, int amount, String msg) {
            player.changeDeposit(g, amount, msg);
        }

        public final void depositOrWithdraw(AbstractPlayer player, Game g, Callback<Object> cb) {
            synchronized (g.lock) {
                player.askHowMuchToDepositOrWithdraw(g, (amount) -> {
                    if (player.cash - amount >= 0 && player.deposit + amount >= 0) {
                        player.cash -= amount;
                        player.deposit += amount;
                    }
                    cb.run(null);
                });
            }
        }

        public final void pay(AbstractPlayer player, Game g, int amount, String msg, Callback<Object> cb) {
            player.pay(g, amount, msg, cb);
        }
    }

    public static final class CardInterface implements Serializable {
        public final void reverse(AbstractPlayer player) {
            player.reversed = !player.reversed;
        }
    }
}
