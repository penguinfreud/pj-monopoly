package monopoly.place;

import monopoly.AbstractPlayer;

public abstract class Place {
    Place prev, next;

    public Place getPrev() {
        return prev;
    }

    public Place getNext() {
        return next;
    }

    public abstract void onLanded(AbstractPlayer player);
}
