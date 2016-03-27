package monopoly;

import monopoly.event.Listener;
import monopoly.place.Property;

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
}
