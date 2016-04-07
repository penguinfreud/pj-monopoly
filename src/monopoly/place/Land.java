package monopoly.place;

import monopoly.GameMap;
import monopoly.Property;

public class Land extends Property {
    static {
        GameMap.registerPlaceReader("Land", (r, sc) -> new Land(sc.next(), sc.nextInt(), r.getStreet(sc.next())));
    }

    private final Street street;

    protected Land(String name, int price, Street street) {
        super(name, price);
        this.street = street;
        street.addLand(this);
    }

    public Street getStreet() {
        return street;
    }

    @Override
    public int getPurchasePrice() {
        return getPrice() * getLevel();
    }

    @Override
    public int getUpgradePrice() {
        return getPrice() / 2;
    }

    @Override
    public int getRent() {
        return getPrice() * 2 / 10 + street.getExtraRent(this);
    }

    @Override
    public int getMortgagePrice() {
        return getPrice() * getLevel();
    }
}
