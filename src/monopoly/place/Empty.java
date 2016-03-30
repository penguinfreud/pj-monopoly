package monopoly.place;

import monopoly.Map;
import monopoly.Place;

public class Empty extends Place {
    static {
        Map.registerPlaceReader("Empty", (r, sc) -> new Empty());
    }

    protected Empty() {
        super("Empty");
    }
}
