package monopoly;

import monopoly.place.Place;
import monopoly.util.Consumer0;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Property extends Place {
    static {
        Game.putDefaultConfig("property-max-level", 6);
    }

    private IPlayer owner;
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

    public IPlayer getOwner() {
        return owner;
    }

    public int getLevel() {
        return level;
    }

    void changeOwner(IPlayer owner) {
        this.owner = owner;
    }

    void upgrade(Game g) {
        if (level < (Integer) g.getConfig("property-max-level")) {
            level++;
        }
    }

    public void resetLevel(Game g) {
        synchronized (g.lock) {
            level = 1;
        }
    }

    public void resetOwner(Game g) {
        synchronized (g.lock) {
            owner = null;
        }
    }

    @Override
    public void arriveAt(Game g, Consumer0 cb) {
        if (g.getState() == Game.State.TURN_LANDED) {
            IPlayer player = g.getCurrentPlayer();
            if (owner == null) {
                Properties.get(player).buyProperty(this, cb);
            } else if (owner == player) {
                if (level < (Integer) g.getConfig("property-max-level")) {
                    Properties.get(player).upgradeProperty(this, cb);
                } else {
                    cb.run();
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
