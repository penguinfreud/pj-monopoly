package monopoly.place;

import monopoly.Game;
import monopoly.Property;
import monopoly.util.Consumer0;

public class DelegatePlace extends Place {
    private final Place place;

    protected DelegatePlace(Place place) {
        super(place.getName());
        this.place = place;
    }

    @Override
    public String getName() {
        return place.getName();
    }

    @Override
    public String toString(Game g) {
        return place.toString(g);
    }

    @Override
    public void arriveAt(Game g, Consumer0 cb) {
        place.arriveAt(g, cb);
    }

    @Override
    public void passBy(Game g, Consumer0 cb) {
        place.passBy(g, cb);
    }

    protected Place getPlace() {
        return place;
    }

    @Override
    public Property asProperty() {
        return place.asProperty();
    }
}
