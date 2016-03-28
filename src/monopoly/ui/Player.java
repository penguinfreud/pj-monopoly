package monopoly.ui;

import monopoly.AbstractPlayer;
import monopoly.Game;
import monopoly.Property;
import monopoly.event.Listener;

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

    @Override
    public void useCards(Game g, Listener<Object> cb) {
        cb.run(null);
    }
}
