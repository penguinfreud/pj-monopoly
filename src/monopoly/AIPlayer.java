package monopoly;

import monopoly.AbstractPlayer;
import monopoly.Game;
import monopoly.Property;
import monopoly.event.Listener;

public class AIPlayer extends AbstractPlayer {
    public AIPlayer() {}

    public AIPlayer(String name) {
        setName(name);
    }

    @Override
    public void askWhetherToBuyProperty(Game g, Listener<Boolean> cb) {
        cb.run(true);
    }

    @Override
    public void askWhetherToUpgradeProperty(Game g, Listener<Boolean> cb) {
        cb.run(true);
    }

    @Override
    public void askWhichPropertyToMortgage(Game g, Listener<Property> cb) {
        cb.run(getProperties().get(0));
    }

    @Override
    public void useCards(Game g, Listener<Object> cb) {
        cb.run(null);
    }
}
