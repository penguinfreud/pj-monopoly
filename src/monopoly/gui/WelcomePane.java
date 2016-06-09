package monopoly.gui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class WelcomePane extends VBox implements IPane {
    public WelcomePane(MainController controller) {
        Button newGameBtn = controller.createButton("New Game", e -> controller.newGame());
        newGameBtn.setMaxWidth(Double.MAX_VALUE);

        Button exitBtn = controller.createButton("Exit", e -> Platform.exit());
        exitBtn.setMaxWidth(Double.MAX_VALUE);

        getChildren().addAll(newGameBtn, exitBtn);
        setAlignment(Pos.CENTER);
        setPrefWidth(200);
        setPadding(new Insets(200));
        setSpacing(20);
    }

    @Override
    public Node center() {
        return this;
    }
}
