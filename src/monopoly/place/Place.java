package monopoly.place;

import monopoly.Game;
import monopoly.GameObject;
import monopoly.IPlayer;
import monopoly.Property;
import monopoly.util.Consumer0;

import java.util.ArrayList;
import java.util.List;

public abstract class Place implements GameObject {
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

    public void init(Game g) {
    }

    public final boolean hasRoadblock() {
        return roadblocks > 0;
    }

    public final void setRoadblock(Game g) {
        synchronized (g.lock) {
            ++roadblocks;
        }
    }

    public final void clearRoadblocks() {
        roadblocks = 0;
    }

    public void arriveAt(Game g, Consumer0 cb) {
        cb.accept();
    }

    public void passBy(Game g, Consumer0 cb) {
        cb.accept();
    }

    public Property asProperty() {
        return null;
    }

    public static int withinReach(Place a, Place b, int steps) {
        Place back = a;
        if (a == b) {
            return 0;
        }
        for (int i = 0; i < steps; i++) {
            a = a.getNext();
            back = back.getPrev();
            if (a == b || back == b) {
                return i + 1;
            }
        }
        return -1;
    }

    public static List<Place> getPlacesWithinReach(Place a, int steps) {
        List<Place> result = new ArrayList<>();
        Place forward = a, backward = a;
        result.add(a);
        for (int i = 0; i < steps; i++) {
            forward = forward.getNext();
            backward = backward.getPrev();
            if (result.indexOf(forward) == -1)
                result.add(forward);
            if (result.indexOf(backward) == -1)
                result.add(0, backward);
        }
        return result;
    }

    public static int withinPlayersReach(IPlayer player, Place place, int steps) {
        Place cur = player.getCurrentPlace();
        for (int i = 0; i < steps; i++) {
            cur = player.isReversed() ? cur.getPrev() : cur.getNext();
            if (cur == place) {
                return i + 1;
            }
        }
        return 0;
    }

    public static List<Place> getPlacesWithinPlayersReach(IPlayer player, int steps) {
        List<Place> result = new ArrayList<>();
        Place cur = player.getCurrentPlace();
        for (int i = 0; i < steps; i++) {
            cur = player.isReversed() ? cur.getPrev() : cur.getNext();
            if (result.indexOf(cur) >= 0)
                break;
            result.add(cur);
        }
        return result;
    }

    public static void loadAll() throws ClassNotFoundException {
        Class.forName("monopoly.place.Empty");
        Class.forName("monopoly.place.Land");
        Class.forName("monopoly.place.News");
        Class.forName("monopoly.place.Bank");
        Class.forName("monopoly.place.CouponSite");
        Class.forName("monopoly.place.CardSite");
        Class.forName("monopoly.place.CardShop");
        Class.forName("monopoly.place.Trap");
        Class.forName("monopoly.place.Hospital");
    }
}
