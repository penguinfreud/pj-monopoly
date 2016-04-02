package monopoly;

import monopoly.async.Callback;

import java.io.Serializable;

public abstract class Place extends GameObject implements Serializable {
    private String name;
    Place prev, next;
    private int roadblocks = 0;

    protected Place(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString(Game g) {
        return g.getText("place_" + name.toLowerCase());
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
        cb.run(g, null);
    }

    public void onPassingBy(Game g, AbstractPlayer.PlaceInterface pi, Callback<Object> cb) {
        cb.run(g, null);
    }
    
    public Property asProperty() {
        return null;
    }

    public static int withinReach(Place a, Place b, int steps) {
        Place back = a;
        if (a == b) {
            return 0;
        }
        for (int i = 0; i<steps; i++) {
            a = a.getNext();
            back = back.getPrev();
            if (a == b || back == b) {
                return i + 1;
            }
        }
        return -1;
    }

    public static int withPlayersReach(AbstractPlayer player, Place place, int steps) {
        Place cur = player.getCurrentPlace();
        for (int i = 0; i<steps; i++) {
            cur = player.isReversed()? cur.getPrev(): cur.getNext();
            if (cur == place) {
                return i + 1;
            }
        }
        return 0;
    }
}
