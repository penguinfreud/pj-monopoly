package monopoly;

public abstract class Property extends Place {
    private AbstractPlayer owner;
    private int price = 0, level = 0;

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
        level++;
    }

    void mortgage() {
        owner = null;
    }

    @Override
    public void onLanded(Game g) {
        if (g.getState() == Game.State.TURN_LANDED) {
            AbstractPlayer p = g.getCurrentPlayer();
            if (owner == null) {
                p.askWhetherToBuyProperty(g, (ok) -> {
                    if (ok) p.buyProperty(g);
                    g.endTurn();
                });
            } else if (owner == p) {
                p.askWhetherToUpgradeProperty(g, (ok) -> {
                    if (ok) p.upgradeProperty(g);
                    g.endTurn();
                });
            } else {
                p.payRent(g);
                g.endTurn();
            }
        }
    }
    
    @Override
    public Property asProperty() {
        return this;
    }
}
