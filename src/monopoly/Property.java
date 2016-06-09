package monopoly;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;
import monopoly.place.Place;
import monopoly.util.Consumer0;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Property extends Place {
    static {
        Game.putDefaultConfig("property-max-level", 6);
    }

    private final ObjectProperty<IPlayer> owner = new SimpleObjectProperty<>();
    private final DoubleProperty price = new SimpleDoubleProperty(0);
    private final IntegerProperty level = new SimpleIntegerProperty(1);

    protected Property(String name, double price) {
        super(name);
        this.price.set(price);
    }

    @Override
    public String toString(Game g) {
        return g.getText(getName());
    }

    @Override
    public void init(Game g) {
        Properties.enable(g);
    }

    public final boolean isFree() {
        return owner.get() == null;
    }

    public DoubleProperty priceProperty() {
        return price;
    }

    public final double getPrice() {
        return price.get();
    }

    private DoubleBinding createZeroBinding() {
        return Bindings.createDoubleBinding(() -> 0.0);
    }

    public DoubleBinding purchasePrice() {
        return createZeroBinding();
    }

    public final double getPurchasePrice() {
        return purchasePrice().get();
    }

    public DoubleBinding upgradePrice() {
        return createZeroBinding();
    }

    public final double getUpgradePrice() {
        return upgradePrice().get();
    }

    public DoubleBinding rent() {
        return createZeroBinding();
    }

    public final double getRent() {
        return rent().get();
    }

    public DoubleBinding mortgagePrice() {
        return createZeroBinding();
    }

    public final double getMortgagePrice() {
        return mortgagePrice().get();
    }

    public ObjectProperty<IPlayer> ownerProperty() {
        return owner;
    }

    public final IPlayer getOwner() {
        return owner.get();
    }

    public IntegerProperty levelProperty() {
        return level;
    }

    public final int getLevel() {
        return level.get();
    }

    void upgrade(Game g) {
        if (level.get() < (Integer) g.getConfig("property-max-level")) {
            level.set(level.get() + 1);
        }
    }

    public void resetLevel(Game g) {
        synchronized (g.lock) {
            level.set(1);
        }
    }

    public void resetOwner(Game g) {
        synchronized (g.lock) {
            owner.set(null);
        }
    }

    @Override
    public void arriveAt(Game g, Consumer0 cb) {
        if (g.getState() == Game.State.TURN_LANDED) {
            IPlayer player = g.getCurrentPlayer();
            if (owner.get() == null) {
                Properties.get(player).buyProperty(this, cb);
            } else if (owner.get() == player) {
                if (level.get() < (Integer) g.getConfig("property-max-level")) {
                    Properties.get(player).upgradeProperty(this, cb);
                } else {
                    cb.accept();
                }
            } else {
                Properties.get(player).payRent(this, cb);
            }
        } else {
            Logger.getAnonymousLogger().log(Level.WARNING, Game.WRONG_STATE);
        }
    }

    @Override
    public Property asProperty() {
        return this;
    }
}
