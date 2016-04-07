package monopoly.place;

import monopoly.GameMap;
import monopoly.Place;

public class Empty extends Place {
    static {
        GameMap.registerPlaceReader("Empty", (r, sc) -> new Empty());
    }

    private Empty() {
        super("Empty");
    }
}
