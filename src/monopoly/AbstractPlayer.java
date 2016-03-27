package monopoly;

import monopoly.event.Action;
import monopoly.event.Event;
import monopoly.event.Listener;
import monopoly.place.Place;
import monopoly.place.Property;

import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractPlayer {
    private final Object lock = new Object();

    private Place currentPlace;
    private int cash, deposit;
    private CopyOnWriteArrayList<Property> properties = new CopyOnWriteArrayList<>();

    final void initPlace(Place place) {
        currentPlace = place;
    }

    final void initCash(int cash) {
        this.cash = cash;
    }

    final void initDeposit(int deposit) {
        this.deposit = deposit;
    }

    public final int getCash() {
        return cash;
    }

    public final int getDeposit() {
        return deposit;
    }

    public final boolean owns(Property prop) {
        return properties.contains(prop);
    }

    protected void beginTurn(Game g) {
        g.rollTheDice();
    }

    public abstract void askWhetherToBuyProperty(Game g);
    public abstract void askWhetherToUpgradeProperty(Game g);

    private void changeCash(Game g, int amount) {
        synchronized (lock) {
            cash += amount;
            g.triggerCashChange(new Game.CashChangeEvent(this, amount));
        }
    }

    private void sellProperties() {}

    public void payRent(Game g) {
        synchronized (lock) {
            int rent = ((Property) currentPlace).getRent();
            changeCash(g, -rent);
            if (cash < 0) {
                if (cash + deposit >= 0) {
                    cash = 0;
                    deposit += cash;
                } else {
                    cash += deposit;
                    deposit = 0;
                    sellProperties();
                    if (cash < 0) {
                        g.triggerBankrupt(this);
                    }
                }
            }
        }
    }
}
