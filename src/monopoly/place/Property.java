package monopoly.place;

import monopoly.AbstractPlayer;
import monopoly.Game;

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
        return price;
    }

    public int getUpgradePrice() {
        return price / 2;
    }

    public int getRent() {
        return price * 3 / 10;
    }

    public int getMortgagePrice() {
        return price;
    }

    public AbstractPlayer getOwner() {
        return owner;
    }

    public int getLevel() {
        return level;
    }

    public void changeOwner(AbstractPlayer.Promise promise) {
        owner = promise.getOwner();
    }

    public void upgrade(AbstractPlayer.Promise promise) {
        if (promise.getOwner() == owner) {
            level++;
        }
    }

    public void mortgage(AbstractPlayer.Promise promise) {
        if (promise.getOwner() == owner) {
            owner = null;
        }
    }

    @Override
    public void onLanded(Game g) {
        AbstractPlayer p = g.getCurrentPlayer();
        if (owner == null) {
            p.askWhetherToBuyProperty(g, (ok) -> {
                if (ok) p.buyProperty(g);
            });
        } else if (owner == p) {
            p.askWhetherToUpgradeProperty(g, (ok) -> {
                if (ok) p.upgradeProperty(g);
            });
        } else {
            p.payRent(g);
        }
    }
}
