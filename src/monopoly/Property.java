package monopoly;

import monopoly.util.Consumer0;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Property extends Place {
    static {
        Game.putDefaultConfig("property-max-level", 6);
    }

    private static final Logger logger = Logger.getLogger(Property.class.getName());

    private AbstractPlayer owner;
    private int price = 0, level = 1;

    protected Property(String name, int price) {
        super(name);
        this.price = price;
    }

    @Override
    public String toString(Game g) {
        return g.getText(getName());
    }

    public boolean isFree() {
        return owner == null;
    }

    public int getPrice() {
        return price;
    }

    public int getPurchasePrice() {
        return 0;
    }

    public int getUpgradePrice() {
        return 0;
    }

    public int getRent() {
        return 0;
    }

    public int getMortgagePrice() {
        return 0;
    }

    public AbstractPlayer getOwner() {
        return owner;
    }

    public int getLevel() {
        return level;
    }

    void changeOwner(AbstractPlayer owner) {
        this.owner = owner;
    }

    void upgrade(Game g) {
        if (level < (Integer) g.getConfig("property-max-level")) {
            level++;
        }
    }

    void resetLevel() {
        level = 1;
    }

    void resetOwner() {
        owner = null;
    }

    @Override
    public void onLanded(Game g, PlaceInterface pi, Consumer0 cb) {
        if (g.getState() == Game.State.TURN_LANDED) {
            AbstractPlayer p = g.getCurrentPlayer();
            if (owner == null) {
                Properties.get(p).buyProperty(this, cb);
            } else if (owner == p) {
                if (level < (Integer) g.getConfig("property-max-level")) {
                    Properties.get(p).upgradeProperty(this, cb);
                } else {
                    cb.run();
                }
            } else {
                Properties.get(p).payRent(this, cb);
            }
        } else {
            logger.log(Level.WARNING, Game.WRONG_STATE);
        }
    }
    
    @Override
    public Property asProperty() {
        return this;
    }
}
