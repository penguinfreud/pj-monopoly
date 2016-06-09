package monopoly.gui;

import javafx.application.Application;
import javafx.stage.Stage;
import monopoly.place.Place;

public final class Main extends Application {
    private final MainController controller = new MainController();

    @Override
    public void start(Stage primaryStage) throws Exception {
        controller.initStage(primaryStage);
    }

    public static void main(String[] args) throws ClassNotFoundException {
        Class.forName("monopoly.gui.GUIGameMap");
        Place.loadAll();
        launch(args);
    }
}
