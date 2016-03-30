package monopoly.gui;

import monopoly.Map;
import monopoly.MapReader;
import monopoly.Place;
import monopoly.PlaceReader;
import monopoly.async.Function;

import java.util.Scanner;

public class GUIMapReader extends MapReader {
    protected GUIMapReader() {}

    static {
        Map.registerMapReader("GUIMap", new GUIMapReader());
    }
}
