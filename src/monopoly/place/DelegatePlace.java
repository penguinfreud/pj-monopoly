package monopoly.place;

import monopoly.AbstractPlayer;
import monopoly.Game;
import monopoly.Place;
import monopoly.Property;
import monopoly.util.Callback;

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
    public void onLanded(Game g, AbstractPlayer.PlaceInterface pi, Callback<Object> cb) {
        place.onLanded(g, pi, cb);
    }

    @Override
    public void onPassingBy(Game g, AbstractPlayer.PlaceInterface pi, Callback<Object> cb) {
        place.onPassingBy(g, pi, cb);
    }

    protected Place getPlace() {
        return place;
    }

    @Override
    public Property asProperty() {
        return place.asProperty();
    }
}
