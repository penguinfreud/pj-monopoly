package monopoly.gui;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.text.Text;
import monopoly.IPlayer;

public class GamePane implements IPane {
    private MainController controller;

    private Group mapView;

    public GamePane(MainController controller) {
        this.controller = controller;
    }

    @Override
    public Node center() {
        return mapView;
    }

    @Override
    public void onShow() {
        mapView = controller.getMap().createMapView();
    }
}
