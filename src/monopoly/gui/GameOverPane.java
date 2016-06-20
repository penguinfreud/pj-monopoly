package monopoly.gui;

import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import monopoly.IPlayer;

public class GameOverPane extends VBox implements IPane {
    private Text winnerText;
    private MainController controller;

    public GameOverPane(MainController controller) {
        this.controller = controller;
        winnerText = controller.createText("");
        getChildren().addAll(winnerText,
                controller.createButton("ok", e -> controller.welcome()));
    }

    public void setWinner(IPlayer player) {
        winnerText.setText(controller.format("game_over_the_winner_is", player.getName()));
    }

    @Override
    public Node center() {
        return this;
    }
}
