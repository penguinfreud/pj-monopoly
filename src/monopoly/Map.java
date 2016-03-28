package monopoly;

import monopoly.event.Function;

import java.io.*;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

public class Map implements Serializable {
    private List<Place> places = new CopyOnWriteArrayList<>();
    private String name;

    Map() {}

    public int size() {
        return places.size();
    }
    
    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    void addPlace(Place place) {
        Place head, tail;
        if (places.isEmpty()) {
            head = place;
            tail = place;
        } else {
            head = places.get(0);
            tail = places.get(places.size() - 1);
        }
        places.add(place);
        tail.next = place;
        place.prev = tail;
        place.next = head;
        head.prev = place;
    }

    public static Map readMap(InputStream is) throws Exception {
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        Scanner sc = new Scanner(isr);
        sc.useDelimiter(Pattern.compile("\\s*,\\s*"));
        String mapType = sc.next();
        MapReader reader = mapReaders.get(mapType);
        Map map = null;

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
        return places.get(0);
    }

    private static final java.util.Map<String, Function<Scanner, Place>> placeReaders = new Hashtable<>();

    public static void registerPlaceReader(String id, Function<Scanner, Place> reader) {
        placeReaders.put(id, reader);
    }

    public static Function<Scanner, Place> getPlaceReader(String id) {
        return placeReaders.get(id);
    }

    private static java.util.Map<String, MapReader> mapReaders = new Hashtable<>();

    public static void registerMapReader(String id, MapReader reader) {
        mapReaders.put(id, reader);
    }
}
