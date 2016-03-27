package monopoly.place;

import monopoly.event.Function;

import java.io.*;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Map implements Serializable {
    private Place[] places;
    private int _size;

    public Map(int size) {
        _size = size;
        places = new Place[size];
    }

    public int size() {
        return _size;
    }

    public static Map fromFile(File fin) throws Exception {
        FileInputStream fis = new FileInputStream(fin);
        InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
        Scanner sc = new Scanner(isr);
        sc.useDelimiter(Pattern.compile("\\s*,\\s*"));
        int size = sc.nextInt();

        Map map = new Map(size);
        Place prev = null, next;
        for (int i = 0; i<size; ++i) {
            String id = sc.next();
            if (placeTypes.containsKey(id)) {
                next = map.places[i] = placeTypes.get(id).run(sc);
                if (prev != null) {
                    prev.next = next;
                    next.prev = prev;
                }
                prev = next;
            } else {
                throw new Exception("Unknown place type: '" + id + "'");
            }
        }

        if (size > 0) {
            next = map.places[0];
            prev.next = next;
            next.prev = prev;
        }

        sc.close();
        isr.close();
        fis.close();
        return map;
    }

    public Place getStartingPoint() {
        if (_size > 0)
            return places[0];
        return null;
    }

    private static java.util.Map<String, Function<Scanner, Place>> placeTypes = new Hashtable<>();

    public static void registerPlaceType(String id, Function<Scanner, Place> reader) {
        placeTypes.put(id, reader);
    }

    static {
        try {
            Class.forName("monopoly.place.Street");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
