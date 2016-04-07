package monopoly;

import java.io.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

public class GameMap implements Serializable {
    private Place head = null;
    private String name = null;
    private int _size = 0;

    public GameMap() {}

    public int size() {
        return _size;
    }
    
    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    protected void addPlace(Place place) {
        Place tail;
        if (head == null) {
            head = place;
            head.prev = head;
            head.next = head;
        } else {
            tail = head.prev;
            tail.next = place;
            place.prev = tail;
            place.next = head;
            head.prev = place;
        }
        ++_size;
    }

    public static GameMap readMap(InputStream is) throws Exception {
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        Scanner sc = new Scanner(isr);
        sc.useDelimiter(Pattern.compile("\\s*(,|[\r\n]+)\\s*"));
        String mapType = sc.next();
        GameMapReader reader = mapReaders.get(mapType);
        GameMap map;

        if (reader != null) {
            map = reader.readMap(sc);
        } else {
            throw new Exception("Unknown map type: '" + mapType + "'");
        }

        sc.close();
        isr.close();
        return map;
    }

    public Place getStartingPoint() {
        return head;
    }

    private static final Map<String, PlaceReader> placeReaders = new Hashtable<>();

    public static void registerPlaceReader(String id, PlaceReader reader) {
        placeReaders.put(id, reader);
    }

    public static PlaceReader getPlaceReader(String id) {
        return placeReaders.get(id);
    }

    private static final Map<String, GameMapReader> mapReaders = new Hashtable<>();

    public static void registerMapReader(String id, GameMapReader reader) {
        mapReaders.put(id, reader);
    }
}
