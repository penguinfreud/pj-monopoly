package monopoly;

import monopoly.event.Action;
import monopoly.event.Event;
import monopoly.event.Listener;
import monopoly.place.Place;
import monopoly.place.Property;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPlayer {
    private Place currentPlace;
    private int cash, deposit;
    private ArrayList<Property> properties = new ArrayList<>();

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
        cash += amount;
        g.triggerCashChange(new Game.CashChangeEvent(this, amount));
    }

    private void sellProperties() {}

    public void payRent(Game g) {
        int rent = ((Property)currentPlace).getRent();
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
