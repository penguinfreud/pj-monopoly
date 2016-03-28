package monopoly;

import monopoly.event.Listener;

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

    public void use(Game g, Listener<Object> cb) {
        cb.run(null);
    }
}
