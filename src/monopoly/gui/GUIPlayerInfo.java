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
import monopoly.util.Parasite;

public class GUIPlayerInfo {
    private static final Parasite<Game, Boolean> enabled = new Parasite<>();
    private static final Parasite<IPlayer, GUIPlayerInfo> parasites = new Parasite<>();

    public static GUIPlayerInfo get(IPlayer player) {
        return parasites.get(player);
    }

    public static void enable(Game game, MainController controller) {
        if (enabled.get(game) == null) {
            enabled.set(game, true);
            BasePlayer.onAddPlayer.get(game).addListener(player -> {
                parasites.set(player, new GUIPlayerInfo(controller));
            });
        }
    }

    private MainController controller;
    private IntegerProperty iconIndex = new SimpleIntegerProperty();
    private ObjectProperty<Color> color = new SimpleObjectProperty<>();
    private Node token = null;

    public GUIPlayerInfo(MainController controller) {
        this.controller = controller;
    }

    public ObjectBinding<Image> icon(int size) {
        return Bindings.createObjectBinding(
                () -> controller.getImageManager().getImage("/icons/characters/" +
                    size + "x" + size + "/" + (iconIndex.get()+ 1) + ".png"), iconIndex);
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
            token = circle;
        }
        return token;
    }
}
