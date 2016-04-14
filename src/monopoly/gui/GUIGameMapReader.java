package monopoly.gui;

import monopoly.place.GameMap;
import monopoly.place.GameMapReader;

public class GUIGameMapReader extends GameMapReader {
    protected GUIGameMapReader() {}

    static {
        GameMap.registerMapReader("GUIGameMap", new GUIGameMapReader());
    }
}
