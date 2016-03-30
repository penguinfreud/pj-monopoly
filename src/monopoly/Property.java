package monopoly;

import monopoly.async.Callback;

public abstract class Property extends Place {
    private AbstractPlayer owner;
    private int price = 0, level = 1;
    private static final int MAX_LEVEL = 6;

    protected Property(String name, int price) {
        super(name);
        this.price = price;
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

    void upgrade() {
        if (level < MAX_LEVEL) {
            level++;
        }
    }

    void mortgage() {
        owner = null;
    }

    @Override
    public void onLanded(Game g, Callback<Object> cb) {
        if (g.getState() == Game.State.TURN_LANDED) {
            AbstractPlayer p = g.getCurrentPlayer();
            if (owner == null) {
                p.askWhetherToBuyProperty(g, (ok) -> {
                    if (ok) p.buyProperty(g);
                    cb.run(null);
                });
            } else if (owner == p) {
                if (level < MAX_LEVEL) {
                    p.askWhetherToUpgradeProperty(g, (ok) -> {
                        if (ok) p.upgradeProperty(g);
                        cb.run(null);
                    });
                }
            } else {
                p.payRent(g);
                cb.run(null);
            }
        }
    }
    
    @Override
    public Property asProperty() {
        return this;
    }
}
