package monopoly;

import monopoly.async.Callback;
import monopoly.async.CashChangeEvent;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractPlayer implements Serializable {
    private String name;
    private Place currentPlace;
    private int cash, deposit;
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

    private Callback<Object> useCardCb;
    private Callback<Card> selectCardCb = (g, card) -> {
        synchronized (g.lock) {
            if (g.getState() == Game.State.TURN_STARTING) {
                if (card == null) {
                    g.rollTheDice();
                } else {
                    card.use(g, useCardCb);
                }
            }
        }
    };
    
    {
        useCardCb = (g, o) -> {
            synchronized (g.lock) {
                if (g.getState() == Game.State.TURN_STARTING) {
                    askWhichCardToUse(g, selectCardCb);
                }
            }
        };
    }

    final void startTurn(Game g) {
        useCardCb.run(g, null);
    }

    public abstract void askWhetherToBuyProperty(Game g, Callback<Boolean> cb);
    public abstract void askWhetherToUpgradeProperty(Game g, Callback<Boolean> cb);
    public abstract void askWhichPropertyToMortgage(Game g, Callback<Property> cb);
    public abstract void askWhichCardToUse(Game g, Callback<Card> cb);

    private void _changeCash(Game g, int amount) {
        synchronized (g.lock) {
            cash += amount;
            g.triggerCashChange(new CashChangeEvent(this, amount));
        }
    }

    private void sellProperties(Game g, Property prop) {
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
                    askWhichPropertyToMortgage(g, (_g, nextProp) -> sellProperties(g, nextProp));
                } else {
                    g.triggerBankrupt(this);
                }
            }
        }
    }

    final void buyProperty(Game g) {
        synchronized (g.lock) {
            if (g.getState() == Game.State.TURN_LANDED) {
                Property prop = currentPlace.asProperty();
                int price = prop.getPurchasePrice();
                if (prop.isFree() && cash > price) {
                    _changeCash(g, -price);
                    properties.add(prop);
                    prop.changeOwner(this);
                }
            }
        }
    }

    final void upgradeProperty(Game g) {
        synchronized (g.lock) {
            if (g.getState() == Game.State.TURN_LANDED) {
                Property prop = currentPlace.asProperty();
                int price = prop.getUpgradePrice();
                if (prop.isFree() && cash > price) {
                    _changeCash(g, -price);
                    prop.upgrade();
                }
            }
        }
    }

     final void payRent(Game g) {
        synchronized (g.lock) {
            if (g.getState() == Game.State.TURN_LANDED) {
                System.out.println("pay rent");
                int rent = currentPlace.asProperty().getRent();
                _changeCash(g, -rent);
                if (cash < 0) {
                    if (cash + deposit >= 0) {
                        cash = 0;
                        deposit += cash;
                    } else {
                        cash += deposit;
                        deposit = 0;
                        sellProperties(g, null);
                        if (cash < 0) {
                            g.triggerBankrupt(this);
                        }
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
                _startStep(g);
            }
        }
    }

    protected void _startStep(Game g) {
        step(g);
    }

    protected final void step(Game g) {
        synchronized (g.lock) {
            if (g.getState() == Game.State.TURN_WALKING) {
                System.out.println("step " + getCurrentPlace().getName());
                currentPlace = currentPlace.getNext();
                if (--stepsToAdvance == 0) {
                    g.endWalking();
                } else {
                    currentPlace.onPassingBy(g);
                    _startStep(g);
                }
            }
        }
    }
}
