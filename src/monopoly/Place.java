package monopoly;

import monopoly.util.Consumer0;
import monopoly.util.Parasite;

import java.io.Serializable;

public abstract class Place implements Serializable, GameObject {
    private final String name;
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

    public final void setRoadblock(Game g) {
        synchronized (g.lock) {
            ++roadblocks;
        }
    }

    final void clearRoadblocks() {
        roadblocks = 0;
    }

    protected void arriveAt(Game g, Consumer0 cb) {
        cb.run();
    }

    protected void passBy(Game g, Consumer0 cb) {
        cb.run();
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

    public static int withinPlayersReach(AbstractPlayer player, Place place, int steps) {
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
