package monopoly;

import monopoly.async.Function;

import java.util.Scanner;

public class MapReader {
    protected MapReader() {}

    public Map readMap(Scanner sc) throws Exception {
        Map map = new Map();
        map.setName(sc.next());
        while (sc.hasNext()) {
            map.addPlace(readPlace(sc));
        }
        return map;
    }

    public Place readPlace(Scanner sc) throws Exception {
        String id = sc.next();
        PlaceReader reader = Map.getPlaceReader(id);
        if (reader != null) {
            return reader.read(this, sc);
        } else {
            throw new Exception("Unknown place type: '" + id + "'");
        }
    }

    static {
        Map.registerMapReader("Map", new MapReader());
    }
}
