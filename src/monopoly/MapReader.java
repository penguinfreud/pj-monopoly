package monopoly;

import monopoly.event.Function;

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

    protected Place readPlace(Scanner sc) throws Exception {
        String id = sc.next();
        Function<Scanner, Place> reader = Map.getPlaceReader(id);
        if (reader != null) {
            return reader.run(sc);
        } else {
            throw new Exception("Unknown place type: '" + id + "'");
        }
    }

    static {
        Map.registerMapReader("Map", new MapReader());
    }
}
