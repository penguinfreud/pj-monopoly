package monopoly;

import java.io.*;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Map implements Serializable {
    private Place head = null;
    private String name;
    private int _size = 0;

    Map() {}

    public int size() {
        return _size;
    }
    
    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    void addPlace(Place place) {
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

    public static Map readMap(InputStream is) throws Exception {
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        Scanner sc = new Scanner(isr);
        sc.useDelimiter(Pattern.compile("\\s*,\\s*"));
        String mapType = sc.next();
        MapReader reader = mapReaders.get(mapType);
        Map map;

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

    private static final java.util.Map<String, PlaceReader> placeReaders = new Hashtable<>();

    public static void registerPlaceReader(String id, PlaceReader reader) {
        placeReaders.put(id, reader);
    }

    public static PlaceReader getPlaceReader(String id) {
        return placeReaders.get(id);
    }

    private static java.util.Map<String, MapReader> mapReaders = new Hashtable<>();

    public static void registerMapReader(String id, MapReader reader) {
        mapReaders.put(id, reader);
    }
}
