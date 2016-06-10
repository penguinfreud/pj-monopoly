package monopoly.gui;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import monopoly.IPlayer;

public class GamePane implements IPane {
    private MainController controller;

    private Group mapView = new Group();
    private VBox rightPane = new VBox();

    public GamePane(MainController controller) {
        this.controller = controller;

        createRightPane();
    }

    @Override
    public Node center() {
        return mapView;
    }

    @Override
    public Node right() {
        return rightPane;
    }

    @Override
    public void onShow() {
        mapView.getChildren().addAll(controller.getMap().createMapView());
        for (IPlayer player : controller.getGame().getPlayers()) {
            mapView.getChildren().add(GUIPlayerInfo.get(player).getToken());
        }
    }

    private void createRightPane() {
        rightPane.getChildren().addAll(CurrentPlayerInfoPane.get(controller),
                DiceView.get(controller));
        rightPane.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                new BorderWidths(0, 0, 0, 1))));
    }
}
