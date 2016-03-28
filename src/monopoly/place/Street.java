package monopoly.place;

import monopoly.Map;
import monopoly.Property;

public class Street extends Property {
    static {
        Map.registerPlaceType("Street", (sc) -> new Street(sc.next(), sc.nextInt()));
    }

    protected Street(String name, int price) {
        super(name, price);
    }
}
