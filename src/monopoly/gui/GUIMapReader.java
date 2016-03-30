package monopoly.gui;

import monopoly.Map;
import monopoly.MapReader;

public class GUIMapReader extends MapReader {
    protected GUIMapReader() {}

    static {
        Map.registerMapReader("GUIMap", new GUIMapReader());
    }
}
