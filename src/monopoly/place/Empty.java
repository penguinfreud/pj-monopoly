package monopoly.place;

import monopoly.Map;
import monopoly.Place;

public class Empty extends Place {
    protected Empty() {
        super("Empty");
    }

    static {
        Map.registerPlaceType("Empty", (sc) -> new Empty());
    }
}
