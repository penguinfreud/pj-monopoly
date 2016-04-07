package monopoly.gui;

import monopoly.GameMap;
import monopoly.GameMapReader;

public class GUIGameMapReader extends GameMapReader {
    protected GUIGameMapReader() {}

    static {
        GameMap.registerMapReader("GUIGameMap", new GUIGameMapReader());
    }
}
