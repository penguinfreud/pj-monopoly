package monopoly.place;

import monopoly.Map;
import monopoly.Place;
import monopoly.event.Function;

import java.util.Scanner;

public class Empty extends Place {
    protected Empty() {
        super("Empty");
    }

    static {
        Map.registerPlaceType("Empty", (sc) -> new Empty());
    }
}
