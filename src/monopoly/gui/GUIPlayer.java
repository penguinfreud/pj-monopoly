package monopoly.gui;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import monopoly.*;
import monopoly.util.Consumer0;
import monopoly.util.Parasite;

public class GUIPlayer extends BasePlayer implements Properties.IPlayerWithProperties, IPlayerWithCardsAndStock {
    private MainController controller;

    public GUIPlayer(Game g, MainController controller) {
        super(g);
        this.controller = controller;
    }

    @Override
    public void startTurn(Consumer0 cb) {
        DiceView diceView = DiceView.get(controller);
        EventHandler<MouseEvent> listener = e -> {
            diceView.setOnMouseClicked(null);
            cb.accept();
        };
        diceView.setOnMouseClicked(listener);
    }
}
