package monopoly;

import monopoly.async.Callback;

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

    public void onLanded(Game g, AbstractPlayer.PlaceInterface pi, Callback<Object> cb) {
        cb.run(null);
    }

    public void onPassingBy(Game g, AbstractPlayer.PlaceInterface pi, Callback<Object> cb) {
        cb.run(null);
    }
    
    public Property asProperty() {
        return null;
    }
}
