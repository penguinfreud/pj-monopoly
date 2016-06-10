package monopoly.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import monopoly.BasePlayer;
import monopoly.Game;
import monopoly.IPlayer;

import java.util.Hashtable;
import java.util.Map;

public class GUIPlayerInfo {
    static {
        MainController.putDefaultConfig("player-colors", new Color[]{
                Color.BLUE, Color.YELLOW, Color.GREEN, Color.RED,
                Color.BROWN, Color.AZURE, Color.CHOCOLATE, Color.FIREBRICK,
                Color.PAPAYAWHIP, Color.BISQUE, Color.VIOLET, Color.TAN
        });
    }

    private static final Map<Game, Boolean> enabled = new Hashtable<>();
    private static final Map<IPlayer, GUIPlayerInfo> parasites = new Hashtable<>();

    public static GUIPlayerInfo get(IPlayer player) {
        return parasites.get(player);
    }

    public static void enable(Game game, MainController controller) {
        if (enabled.get(game) == null) {
            enabled.put(game, true);
            BasePlayer.onAddPlayer.get(game).addListener(player -> {
                parasites.put(player, new GUIPlayerInfo(player, controller));
            });
            game.onGameStart.addListener(() -> {
                Color[] colors = controller.getConfig().get("player-colors");
                int i = 0;
                for (IPlayer player : game.getPlayers()) {
                    get(player).setColor(colors[i]);
                    i++;
                }
            });
        }
    }

    private MainController controller;
    private IPlayer player;
    private IntegerProperty iconIndex = new SimpleIntegerProperty();
    private ObjectProperty<Color> color = new SimpleObjectProperty<>();
    private Node token = null;

    public GUIPlayerInfo(IPlayer player, MainController controller) {
        this.player = player;
        this.controller = controller;
    }

    public IntegerProperty iconIndexProperty() {
        return iconIndex;
    }

    public ObjectProperty<Color> colorProperty() {
        return color;
    }

    public int getIconIndex() {
        return iconIndex.get();
    }

    public void setIconIndex(int iconIndex) {
        this.iconIndex.set(iconIndex);
    }

    public Color getColor() {
        return color.get();
    }

    public void setColor(Color color) {
        this.color.set(color);
    }

    public ObjectBinding<Image> icon(int size) {
        return Bindings.createObjectBinding(
                () -> controller.getImageManager().getImage("/icons/characters/" +
                        size + "x" + size + "/" + (iconIndex.get() + 1) + ".png"), iconIndex);
    }

    public Image getIcon(int size) {
        return icon(size).get();
    }

    public Node getToken() {
        if (token == null) {
            Circle circle = new Circle(24);
            circle.setFill(new ImagePattern(getIcon(48)));
            circle.setStroke(color.get());
            circle.setStrokeType(StrokeType.OUTSIDE);
            circle.setStrokeWidth(3);
            circle.translateXProperty().bind(Bindings.createObjectBinding(
                    () -> ((GUIPlace) player.getCurrentPlace()).getX() + 36,
                    player.currentPlaceProperty()));
            circle.translateYProperty().bind(Bindings.createObjectBinding(
                    () -> ((GUIPlace) player.getCurrentPlace()).getY() + 36,
                    player.currentPlaceProperty()));
            token = circle;
        }
        return token;
    }
}
