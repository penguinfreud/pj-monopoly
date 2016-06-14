package monopoly.gui;

import javafx.application.Application;
import javafx.stage.Stage;

public final class Main extends Application {
    private final MainController controller = new MainController();

    public Main() throws ClassNotFoundException {
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        controller.initStage(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
