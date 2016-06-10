package monopoly.gui;

import javafx.scene.Group;
import monopoly.place.GameMap;
import monopoly.place.GameMapReader;

import java.util.function.Consumer;

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

    private Consumer<? super GUIPlace> onSelectPlace;

    public Group createMapView() {
        Group group = new Group();
        GUIPlace start = (GUIPlace) getStartingPoint(), place = start;
        do {
            group.getChildren().add(place.getToken());
            place = (GUIPlace) place.getNext();
        } while (place != start);
        return group;
    }

    void selectPlace(GUIPlace place) {
        if (onSelectPlace != null) {
            Consumer<? super GUIPlace> cb = onSelectPlace;
            onSelectPlace = null;
            cb.accept(place);
        }
    }

    public void setOnSelectPlace(Consumer<? super GUIPlace> onSelectPlace) {
        this.onSelectPlace = onSelectPlace;
    }
}
