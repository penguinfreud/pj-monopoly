package monopoly.place;

import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner;

public class GameMapReader {
    protected GameMapReader() {
    }

    private final Map<String, Street> streetMap = new Hashtable<>();

    public GameMap readMap(Scanner sc) throws Exception {
        GameMap map = createMap();
        initStreetMap();
        map.setName(sc.next());
        while (sc.hasNext()) {
            map.addPlace(readPlace(sc));
        }
        return map;
    }

    protected GameMap createMap() {
        return new GameMap();
    }

    protected void initStreetMap() {
        streetMap.clear();
    }

    public Place readPlace(Scanner sc) throws Exception {
        String id = sc.next();
        PlaceReader reader = GameMap.getPlaceReader(id);
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
        GameMap.registerMapReader("GameMap", new GameMapReader());
    }
}
