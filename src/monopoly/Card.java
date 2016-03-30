package monopoly;

import monopoly.async.Callback;

public class Card {
    private String name;
    private AbstractPlayer owner = null;

    public String getName() {
        return name;
    }

    public AbstractPlayer getOwner() {
        return owner;
    }

    void changeOwner(AbstractPlayer newOwner) {
        owner = newOwner;
    }

    public void use(Game g, Callback<Object> cb) {
        cb.run(null);
    }
}
