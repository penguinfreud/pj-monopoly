package monopoly.gui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import monopoly.*;
import monopoly.card.RobCard;
import monopoly.card.TeardownCard;
import monopoly.gui.dialogs.LocalButtonTypes;
import monopoly.place.GameMap;
import monopoly.place.Place;
import monopoly.stock.StockMarket;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collector;

public class MainController {
    private static Config defaultConfig = new Config();

    static {
        defaultConfig.put("default-maps", new String[]{"from_file", "gui_1.map"});
        defaultConfig.put("icons-count", 12);
        defaultConfig.put("font", new Font(16));
    }

    public static <T> void putDefaultConfig(String key, T value) {
        defaultConfig.put(key, value);
    }

    private Game game = new Game();
    private Config config = new Config(defaultConfig);
    private ObjectProperty<IPlayer> editingPlayer = new SimpleObjectProperty<>();

    private ResourceBundle messages;

    private Stage stage;

    private ImageManager imageManager = new ImageManager();
    private GUIGameMap.GUIGameMapReader mapReader = new GUIGameMap.GUIGameMapReader(this);
    private LocalButtonTypes buttonTypes;

    private IPane currentPane;
    private BorderPane rootPane = new BorderPane();
    private IPane welcomePane, gameEditorPane, playerEditorPane, gamePane;

    MainController() throws ClassNotFoundException {
        Locale locale = Locale.forLanguageTag(game.getConfig("locale"));
        messages = ResourceBundle.getBundle("messages/ui_messages", locale);

        Properties.enable(game);
        Cards.enable(game);
        StockMarket.enable(game);
        Shareholding.enable(game);
        GUIPlayerInfo.enable(game, this);
        Card.enableAll(game);

        Place.loadAll();
        Class.forName("monopoly.gui.GUIGameMap");
        Class.forName("monopoly.gui.GUIPlace");
        Class.forName("monopoly.gui.GUIProperty");

        buttonTypes = new LocalButtonTypes(this);

        welcomePane = new WelcomePane(this);
        gameEditorPane = new GameEditorPane(this);
        playerEditorPane = new PlayerEditorPane(this);
        gamePane = new GamePane(this);

    }

    void initStage(Stage primaryStage) {
        welcome();
        Scene scene = new Scene(rootPane, 800, 600);
        stage = primaryStage;
        primaryStage.setScene(scene);
        primaryStage.setTitle(getText("monopoly"));

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
                String[] defaultMaps = config.get("default-maps");
                is = MainController.class.getResourceAsStream("/maps/" + defaultMaps[mapIndex]);
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
        Text text = new Text(getText(strText));
        text.setFont(config.get("font"));
        return text;
    }

    public Button createButton(String text, EventHandler<ActionEvent> handler) {
        Button button = new Button(getText(text));
        button.setOnAction(handler);

        button.setFont(config.get("font"));
        button.setPadding(new Insets(8, 10, 8, 10));
        return button;
    }

    public String getCSSFont() {
        Font font = config.get("font");
        return "-fx-font: " + font.getSize() + " " + font.getFamily();
    }

    public String getText(String key) {
        return monopoly.util.Util.getText(messages, key);
    }

    public String format(String key, Object... args) {
        return MessageFormat.format(getText(key), args);
    }

    public LocalButtonTypes getButtonTypes() {
        return buttonTypes;
    }
}
