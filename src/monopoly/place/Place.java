package monopoly.place;

import monopoly.Game;
import monopoly.event.Listener;

import java.io.Serializable;

public abstract class Place implements Serializable {
    private String name;
    Place prev, next;

    protected Place(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Place getPrev() {
        return prev;
    }

    public Place getNext() {
        return next;
    }

    public void onLanded(Game g) {}
    public void onPassing(Game g, Listener<Object> cb) {
        cb.run(null);
    }
}
