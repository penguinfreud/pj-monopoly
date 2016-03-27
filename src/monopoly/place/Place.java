package monopoly.place;

import monopoly.AbstractPlayer;
import monopoly.Game;

import java.io.Serializable;

public abstract class Place implements Serializable {
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
