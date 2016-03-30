package monopoly.gui;

import monopoly.AbstractPlayer;
import monopoly.Card;
import monopoly.Game;
import monopoly.Property;
import monopoly.async.Callback;

public class GUIPlayer extends AbstractPlayer {
    public GUIPlayer() {}

    public GUIPlayer(String name) {
        setName(name);
    }

    @Override
    public void askWhetherToBuyProperty(Game g, Callback<Boolean> cb) {

    }

    @Override
    public void askWhetherToUpgradeProperty(Game g, Callback<Boolean> cb) {

    }

    @Override
    public void askWhichPropertyToMortgage(Game g, Callback<Property> cb) {

    }

    @Override
    public void askWhichCardToUse(Game g, Callback<Card> cb) {
        cb.run(null);
    }
}
