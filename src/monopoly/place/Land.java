package monopoly.place;

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
    public double getPurchasePrice() {
        return getPrice() * getLevel();
    }

    @Override
    public double getUpgradePrice() {
        return getPrice() / 2;
    }

    @Override
    public double getRent() {
        return getPrice() * 2 / 10 + street.getExtraRent(this);
    }

    @Override
    public double getMortgagePrice() {
        return getPrice() * getLevel();
    }
}
