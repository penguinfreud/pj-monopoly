package monopoly;

import monopoly.event.Listener;
import monopoly.place.Property;

public class Player extends AbstractPlayer {
    public Player() {}

    public Player(String name) {
        setName(name);
    }

    @Override
    public void askWhetherToBuyProperty(Game g, Listener<Boolean> cb) {

    }

    @Override
    public void askWhetherToUpgradeProperty(Game g, Listener<Boolean> cb) {

    }

    @Override
    public void askWhichPropertyToMortgage(Game g, Listener<Property> cb) {

    }
}
