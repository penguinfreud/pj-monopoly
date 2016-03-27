package monopoly;

import monopoly.event.Listener;
import monopoly.place.Place;
import monopoly.place.Property;

import java.util.ArrayList;

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

    public void beginTurn(Game g, Listener<Action> cb) {
        cb.run(Action.getDiceAction());
    }
}
