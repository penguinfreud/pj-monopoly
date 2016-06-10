package monopoly.gui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import monopoly.place.*;

public class GUIPlace extends DelegatePlace {
    static {
        MainController.putDefaultConfig("place-token-size", 64.0);
        GameMap.registerPlaceReader("GUIPlace", (r, sc) -> new GUIPlace(
                (GUIGameMap) r.getMap(),
                ((GUIGameMap.GUIGameMapReader) r).getController(),
                sc.nextDouble(), sc.nextDouble(), r.readPlace(sc)));
    }

    private GUIGameMap map;
    private MainController controller;
    private DoubleProperty x = new SimpleDoubleProperty(),
            y = new SimpleDoubleProperty();
    private ObjectProperty<String> icon = new SimpleObjectProperty<>();
    private Node token;

    public GUIPlace(GUIGameMap map, MainController controller, double x, double y, Place place) {
        super(place);
        this.map = map;
        this.controller = controller;
        this.x.set(x);
        this.y.set(y);
    }

    public DoubleProperty xProperty() {
        return x;
    }

    public double getX() {
        return x.get();
    }

    public DoubleProperty yProperty() {
        return y;
    }

    public double getY() {
        return y.get();
    }

    public Node getToken() {
        if (token == null) {
            double size = controller.getConfig().get("place-token-size");
            Pane pane = new Pane();
            pane.setStyle("-fx-background-color: white");
            pane.setPrefWidth(size);
            pane.setPrefHeight(size);
            Text nameText = new Text(toString(controller.getGame()));
            nameText.setTranslateY(16);
            pane.getChildren().add(nameText);
            pane.translateXProperty().bind(x);
            pane.translateYProperty().bind(y);
            pane.setOnMouseClicked(e -> map.selectPlace(this));
            token = pane;
        }
        return token;
    }
}
