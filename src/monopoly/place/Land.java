package monopoly.place;

import javafx.beans.binding.DoubleBinding;
import monopoly.Property;

public class Land extends Property {
    static {
        GameMap.registerPlaceReader("Land", (r, sc) -> new Land(sc.next(), sc.nextInt(), r.getStreet(sc.next())));
    }

    private final Street street;
    private DoubleBinding _purchasePrice, _upgradePrice, _rent, _mortgagePrice;

    protected Land(String name, int price, Street street) {
        super(name, price);
        this.street = street;
        street.addLand(this);
        _purchasePrice = priceProperty().multiply(levelProperty());
        _upgradePrice = priceProperty().multiply(0.5);
        _rent = priceProperty().multiply(0.2).add(street.getExtraRent(this));
        _mortgagePrice = priceProperty().multiply(levelProperty());
    }

    public Street getStreet() {
        return street;
    }

    @Override
    public DoubleBinding purchasePrice() {
        return _purchasePrice;
    }

    @Override
    public DoubleBinding upgradePrice() {
        return _upgradePrice;
    }

    @Override
    public DoubleBinding rent() {
        return _rent;
    }

    @Override
    public DoubleBinding mortgagePrice() {
        return _mortgagePrice;
    }
}
