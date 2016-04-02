package monopoly;

import monopoly.util.Callback;

public abstract class Property extends Place {
    static {
        Game.putDefaultConfig("property-max-level", 6);
    }

    private AbstractPlayer owner;
    private int price = 0, level = 1;

    protected Property(String name, int price) {
        super(name);
        this.price = price;
    }

    @Override
    public String toString(Game g) {
        return getName();
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

    void clearLevel(Game g) {
        level = 1;
    }

    void mortgage() {
        owner = null;
    }

    @Override
    public void onLanded(Game g, AbstractPlayer.PlaceInterface pi, Callback<Object> cb) {
        if (g.getState() == Game.State.TURN_LANDED) {
            AbstractPlayer p = g.getCurrentPlayer();
            if (owner == null) {
                p.buyProperty(g, cb);
            } else if (owner == p) {
                if (level < (Integer) g.getConfig("property-max-level")) {
                    p.upgradeProperty(g, cb);
                } else {
                    System.out.println("max level reached");
                    cb.run(g, null);
                }
            } else {
                p.payRent(g, cb);
            }
        }
    }
    
    @Override
    public Property asProperty() {
        return this;
    }
}
