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

    void resetLevel() {
        level = 1;
    }

    void resetOwner() {
        owner = null;
    }

    @Override
    public void onLanded(Game g, AbstractPlayer.PlaceInterface pi, Callback<Object> cb) {
        //System.out.println("property landed");
        if (g.getState() == Game.State.TURN_LANDED) {
            AbstractPlayer p = g.getCurrentPlayer();
            if (owner == null) {
                //System.out.println("buy");
                p.buyProperty(g, cb);
            } else if (owner == p) {
                if (level < (Integer) g.getConfig("property-max-level")) {
                    //System.out.println("upgrade");
                    p.upgradeProperty(g, cb);
                } else {
                    //System.out.println("max level");
                    cb.run(g, null);
                }
            } else {
                //System.out.println("pay rent");
                p.payRent(g, cb);
            }
        } else {
            //System.out.println("state error");
        }
    }
    
    @Override
    public Property asProperty() {
        return this;
    }
}
