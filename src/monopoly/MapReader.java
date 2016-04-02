package monopoly;

import monopoly.place.Street;

import java.util.Hashtable;
import java.util.Scanner;

public class MapReader {
    protected MapReader() {}
    private final java.util.Map<String, Street> streetMap = new Hashtable<>();

    public Map readMap(Scanner sc) throws Exception {
        Map map = createMap();
        initStreetMap();
        map.setName(sc.next());
        while (sc.hasNext()) {
            map.addPlace(readPlace(sc));
        }
        return map;
    }

    protected Map createMap() {
        return new Map();
    }

    protected void initStreetMap() {
        streetMap.clear();
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

    public Street getStreet(String name) {
        Street street = streetMap.get(name);
        if (street == null) {
            street = new Street(name);
            streetMap.put(name, street);
        }
        return street;
    }

    static {
        Map.registerMapReader("Map", new MapReader());
    }
}
