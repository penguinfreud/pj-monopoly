package monopoly.gui;

import monopoly.AbstractPlayer;
import monopoly.Card;
import monopoly.Game;
import monopoly.Property;
import monopoly.event.Listener;

public class GUIPlayer extends AbstractPlayer {
    public GUIPlayer() {}

    public GUIPlayer(String name) {
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
    public void askWhichCardToUse(Game g, Listener<Card> cb) {
        cb.run(null);
    }
}
