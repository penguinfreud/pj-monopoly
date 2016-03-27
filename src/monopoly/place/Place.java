package monopoly.place;

import monopoly.AbstractPlayer;
import monopoly.Game;

public abstract class Place {
    Place prev, next;

    public Place getPrev() {
        return prev;
    }

    public Place getNext() {
        return next;
    }

    public void onLanded(Game g) {}
    public void onPassing(Game g) {}
}
