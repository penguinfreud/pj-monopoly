package monopoly;

import monopoly.event.Listener;
import monopoly.place.Place;
import monopoly.place.Property;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractPlayer implements Serializable {
    private static final Object lock = new Object();

    public static final class Promise implements Serializable {
        private AbstractPlayer owner;

        private Promise(AbstractPlayer owner) {
            this.owner = owner;
        }

        public AbstractPlayer getOwner() {
            return owner;
        }
    }

    private String name;
    private Place currentPlace;
    private int cash, deposit;
    private List<Property> properties = new CopyOnWriteArrayList<>();
    private final Promise iOwnIt = new Promise(this);

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

    public final void setName(String name) {
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

    public final boolean owns(Property prop) {
        return properties.contains(prop);
    }

    public final List<Property> getProperties() {
        return new CopyOnWriteArrayList<>(properties);
    }

    protected void beginTurn(Game g) {
        g.rollTheDice();
    }

    public abstract void askWhetherToBuyProperty(Game g, Listener<Boolean> cb);
    public abstract void askWhetherToUpgradeProperty(Game g, Listener<Boolean> cb);
    public abstract void askWhichPropertyToMortgage(Game g, Listener<Property> cb);

    private void _changeCash(Game g, int amount) {
        synchronized (lock) {
            cash += amount;
            g.triggerCashChange(new Game.CashChangeEvent(this, amount));
        }
    }

    private final void sellProperties() {
        if (properties.size() > 0) {

        }
    }

    public final void buyProperty(Game g) {
        synchronized (lock) {
            Property prop = (Property) currentPlace;
            int price = prop.getPurchasePrice();
            if (prop.isFree() && cash > price) {
                _changeCash(g, -price);
                properties.add(prop);
                prop.changeOwner(iOwnIt);
            }
        }
    }

    public final void upgradeProperty(Game g) {
        synchronized (lock) {
            Property prop = (Property) currentPlace;
            int price = prop.getUpgradePrice();
            if (prop.isFree() && cash > price) {
                _changeCash(g, -price);
                prop.upgrade(iOwnIt);
            }
        }
    }

    public final void payRent(Game g) {
        synchronized (lock) {
            int rent = ((Property) currentPlace).getRent();
            _changeCash(g, -rent);
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
