package monopoly.player;

import monopoly.AbstractPlayer;
import monopoly.Game;
import monopoly.Property;
import monopoly.event.Event;
import monopoly.event.Listener;

public class GuiPlayer extends AbstractPlayer {
    public GuiPlayer() {}

    public GuiPlayer(String name) {
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
