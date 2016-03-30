package monopoly;

import monopoly.async.Callback;

public class AIPlayer extends AbstractPlayer {
    public AIPlayer() {}

    public AIPlayer(String name) {
        setName(name);
    }

    @Override
    public void askWhetherToBuyProperty(Game g, Callback<Boolean> cb) {
        cb.run(g, true);
    }

    @Override
    public void askWhetherToUpgradeProperty(Game g, Callback<Boolean> cb) {
        cb.run(g, true);
    }

    @Override
    public void askWhichPropertyToMortgage(Game g, Callback<Property> cb) {
        cb.run(g, getProperties().get(0));
    }

    @Override
    public void askWhichCardToUse(Game g, Callback<Card> cb) {
        cb.run(g, null);
    }
}
