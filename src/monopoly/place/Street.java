package monopoly.place;

import monopoly.event.Function;

import java.util.Scanner;

public class Street extends Property {
    static {
        Map.registerPlaceType("Street", new Function<Scanner, Place>() {
            @Override
            public Place run(Scanner sc) {
                String name = sc.next();
                int price = sc.nextInt();
                return new Street(name, price);
            }
        });
    }

    protected Street(String name, int price) {
        super(name, price);
    }
}
