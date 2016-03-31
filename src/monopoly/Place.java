package monopoly;

import monopoly.async.Callback;

import java.io.Serializable;

public abstract class Place implements Serializable {
    private String name;
    Place prev, next;
    private int roadblocks = 0;

    protected Place(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public final Place getPrev() {
        return prev;
    }

    public final Place getNext() {
        return next;
    }

    public final boolean hasRoadblock() {
        return roadblocks > 0;
    }

    final void setRoadblock() {
        ++roadblocks;
    }

    final void clearRoadblocks() {
        roadblocks = 0;
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
