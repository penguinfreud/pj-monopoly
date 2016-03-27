package monopoly;

import monopoly.place.Place;

import java.io.Serializable;

public class Map implements Serializable {
    private Place[] places;
    private int size;

    public Map(int size) {
        this.size = size;
        places = new Place[size];
    }

    public static Map fromFile() {
        return null;
    }

    public Place getStartingPoint() {
        return places[0];
    }
}
