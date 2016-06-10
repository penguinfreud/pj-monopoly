package monopoly.gui;

import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import monopoly.IPlayer;
import monopoly.Property;
import monopoly.place.GameMap;

public class GUIProperty extends GUIPlace {
    private Property property;

    static {
        GameMap.registerPlaceReader("GUIProperty", (r, sc) -> new GUIProperty(
                (GUIGameMap) r.getMap(),
                ((GUIGameMap.GUIGameMapReader) r).getController(),
                sc.nextDouble(), sc.nextDouble(), r.readPlace(sc).asProperty()));
    }

    private Pane token = null;

    public GUIProperty(GUIGameMap map, MainController controller, double x, double y, Property property) {
        super(map, controller, x, y, property);
        this.property = property;
    }

    @Override
    public Node getToken() {
        if (token == null) {
            token = (Pane) super.getToken();
            Rectangle rect = new Rectangle(token.getPrefWidth(), 20);
            rect.fillProperty().bind(Bindings.createObjectBinding(
                    () -> {
                        IPlayer owner = property.getOwner();
                        if (owner == null)
                            return Color.WHITE;
                        return GUIPlayerInfo.get(owner).getColor();
                    },
                    property.ownerProperty()));
            token.getChildren().add(0, rect);
        }
        return token;
    }
}
