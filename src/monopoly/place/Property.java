package monopoly.place;

import monopoly.AbstractPlayer;
import monopoly.Game;

public abstract class Property extends Place {
    private AbstractPlayer owner;
    private int level = 0;

    public abstract int getPurchasePrice();

    public abstract int getUpgradePrice();

    public abstract int getRent();

    public AbstractPlayer getOwner() {
        return owner;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public void onLanded(Game g) {
        AbstractPlayer p = g.getCurrentPlayer();
        if (owner == null) {
            p.askWhetherToBuyProperty(g);
        } else if (owner == p) {
            p.askWhetherToUpgradeProperty(g);
        } else {
            p.payRent(g);
        }
    }
}
