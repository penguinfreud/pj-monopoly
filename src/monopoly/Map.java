package monopoly;

import monopoly.event.Function;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Map implements Serializable {
    private List<Place> places = new ArrayList<>();

    private Map() {}

    public int size() {
        return places.size();
    }

    public static Map fromFile(File fin) throws Exception {
        FileInputStream fis = new FileInputStream(fin);
        InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
        Scanner sc = new Scanner(isr);
        sc.useDelimiter(Pattern.compile("\\s*,\\s*"));

        Map map = new Map();
        Place prev = null, next;
        while (sc.hasNext()) {
            String id = sc.next();
            if (!placeTypes.containsKey(id)) {
                Class.forName("monopoly.place." + id);
            }
            if (placeTypes.containsKey(id)) {
                next = placeTypes.get(id).run(sc);
                map.places.add(next);
                if (prev != null) {
                    prev.next = next;
                    next.prev = prev;
                }
                prev = next;
            } else {
                throw new Exception("Unknown place type: '" + id + "'");
            }
        }

        if (!map.places.isEmpty()) {
            next = map.places.get(0);
            prev.next = next;
            next.prev = prev;
        }

        sc.close();
        isr.close();
        fis.close();
        return map;
    }

    public Place getStartingPoint() {
        return places.get(0);
    }

    private static java.util.Map<String, Function<Scanner, Place>> placeTypes = new Hashtable<>();

    public static void registerPlaceType(String id, Function<Scanner, Place> reader) {
        placeTypes.put(id, reader);
    }
}
