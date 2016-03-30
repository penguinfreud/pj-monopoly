package monopoly.gui;

import monopoly.Map;
import monopoly.MapReader;
import monopoly.Place;
import monopoly.event.Function;

import java.util.Scanner;

public class GUIMapReader extends MapReader {
    protected GUIMapReader() {}

    @Override
    protected Place readPlace(Scanner sc) throws Exception {
        String id = sc.next();
        Function<Scanner, Place> reader = Map.getPlaceReader(id);
        if (reader != null) {
            return new GUIPlace(sc.nextInt(), sc.nextInt(), reader.run(sc));
        } else {
            throw new Exception("Unknown place type: '" + id + "'");
        }
    }

    static {
        Map.registerMapReader("GUIMap", new GUIMapReader());
    }
}
