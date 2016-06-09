package monopoly.gui;

import monopoly.BasePlayer;
import monopoly.Game;
import monopoly.IPlayer;
import monopoly.util.Consumer0;
import monopoly.util.Parasite;

public class GUIPlayer extends BasePlayer {
    private MainController controller;

    public GUIPlayer(Game g, MainController controller) {
        super(g);
        this.controller = controller;
    }

    @Override
    public void startTurn(Consumer0 cb) {

    }
}
