package monopoly.place;

import monopoly.Map;
import monopoly.Property;

public class Land extends Property {
    static {
        Map.registerPlaceReader("Land", (r, sc) -> new Land(sc.next(), sc.nextInt()));
    }

    protected Land(String name, int price) {
        super(name, price);
    }
}
