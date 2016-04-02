package monopoly.place;

import monopoly.Map;
import monopoly.Property;

public class Land extends Property {
    static {
        Map.registerPlaceReader("Land", (r, sc) -> new Land(sc.next(), sc.nextInt(), r.getStreet(sc.next())));
    }

    private final Street street;

    protected Land(String name, int price, Street street) {
        super(name, price);
        this.street = street;
        street.addLand(this);
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
