package monopoly.gui;

import javafx.scene.Group;
import monopoly.place.GameMap;
import monopoly.place.GameMapReader;

public class GUIGameMap extends GameMap {
    static {
        try {
            Class.forName("monopoly.gui.GUIPlace");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static class GUIGameMapReader extends GameMapReader {
        private MainController controller;

        public GUIGameMapReader(MainController controller) {
            this.controller = controller;
        }

        @Override
        protected GameMap createMap() {
            return new GUIGameMap();
        }

        public MainController getController() {
            return controller;
        }
    }

    public Group createMapView() {
        Group group = new Group();
        GUIPlace start = (GUIPlace) getStartingPoint(), place = start;
        do {
            group.getChildren().add(place.getToken());
            place = (GUIPlace) place.getNext();
        } while (place != start);
        return group;
    }
}
