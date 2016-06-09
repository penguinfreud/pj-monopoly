package monopoly.gui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import monopoly.Config;
import monopoly.Game;
import monopoly.IPlayer;
import monopoly.place.GameMap;
import monopoly.place.GameMapReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainController {
    private static Config defaultConfig = new Config();

    static {
        ObservableList<String> defaultMaps = FXCollections.observableArrayList();
        defaultMaps.add("From file");
        defaultMaps.add("gui_1.map");
        defaultConfig.put("default-maps", defaultMaps);
        defaultConfig.put("icons-count", 12);
        defaultConfig.put("font", new Font(16));
    }

    public static <T> void putDefaultConfig(String key, T value) {
        defaultConfig.put(key, value);
    }

    private Game game = new Game();
    private Config config = new Config(defaultConfig);
    private ObjectProperty<IPlayer> editingPlayer = new SimpleObjectProperty<>();

    private Stage stage;

    private ImageManager imageManager = new ImageManager();
    private GUIGameMap.GUIGameMapReader mapReader = new GUIGameMap.GUIGameMapReader(this);

    private IPane currentPane;
    private BorderPane rootPane = new BorderPane();
    private IPane welcomePane, gameEditorPane, playerEditorPane, gamePane;

    MainController() {
        welcomePane = new WelcomePane(this);
        gameEditorPane = new GameEditorPane(this);
        playerEditorPane = new PlayerEditorPane(this);
        gamePane = new GamePane(this);

        GUIPlayerInfo.enable(game, this);
    }

    void initStage(Stage primaryStage) {
        welcome();
        Scene scene = new Scene(rootPane, 600, 600);
        stage = primaryStage;
        primaryStage.setScene(scene);
        primaryStage.setTitle("Monopoly");

        primaryStage.show();
    }

    private void switchToPane(IPane pane) {
        if (currentPane != null) {
            currentPane.onHide();
        }
        currentPane = pane;
        pane.onShow();
        rootPane.setTop(pane.top());
        rootPane.setLeft(pane.left());
        rootPane.setRight(pane.right());
        rootPane.setCenter(pane.center());
        rootPane.setBottom(pane.bottom());
    }

    void welcome() {
        switchToPane(welcomePane);
    }

    void newGame() {
        game.reset();
        switchToPane(gameEditorPane);
    }

    void editPlayer(IPlayer player) {
        editingPlayer.set(player);
        switchToPane(playerEditorPane);
    }

    void completeEditingPlayer() {
        switchToPane(gameEditorPane);
    }

    void startGame() {
        int mapIndex = config.get("selected-default-map-index");
        InputStream is;
        try {
            if (mapIndex == 0) {
                is = new FileInputStream((String) config.get("map-file"));
            } else {
                ObservableList<String> defaultMaps = config.get("default-maps");
                is = MainController.class.getResourceAsStream("/maps/" + defaultMaps.get(mapIndex));
            }
            GameMap map = GameMap.readMap(is, mapReader);
            game.setMap(map);
            game.start();
            switchToPane(gamePane);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Util.alertError("Failed to open map file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Util.alertError("Failed to read map: " + e.getMessage());
        }
    }

    public ImageManager getImageManager() {
        return imageManager;
    }

    public Stage getStage() {
        return stage;
    }

    public Game getGame() {
        return game;
    }

    public GUIGameMap getMap() {
        return (GUIGameMap) game.getMap();
    }

    public Config getConfig() {
        return config;
    }

    public ObjectProperty<IPlayer> editingPlayerProperty() {
        return editingPlayer;
    }

    public Text createText(String strText) {
        Text text = new Text(strText);
        text.setFont(config.get("font"));
        return text;
    }

    public Button createButton(String text, EventHandler<ActionEvent> handler) {
        Button button = new Button(text);
        button.setOnAction(handler);

        button.setFont(config.get("font"));
        button.setPadding(new Insets(8, 10, 8, 10));
        return button;
    }
}
