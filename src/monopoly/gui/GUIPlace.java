package monopoly.gui;

import monopoly.*;
import monopoly.async.Callback;

import java.awt.Graphics;

public class GUIPlace extends Place {
    private Place place;
    private int x, y;
    
    public GUIPlace(int x, int y, Place place) {
        super(place.getName());
        this.x = x;
        this.y = y;
        this.place = place;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String getName() {
        return place.getName();
    }

    @Override
    public void onLanded(Game g, AbstractPlayer.PlaceInterface pi, Callback<Object> cb) {
        place.onLanded(g, pi, cb);
    }

    @Override
    public void onPassingBy(Game g, AbstractPlayer.PlaceInterface pi, Callback<Object> cb) {
        place.onPassingBy(g, pi, cb);
    }

    @Override
    public Property asProperty() {
        return place.asProperty();
    }
    
    public void draw(Graphics g) {
    
    }

    static {
        Map.registerPlaceReader("GUIPlace", (r, sc) -> new GUIPlace(sc.nextInt(), sc.nextInt(), r.readPlace(sc)));
    }
}
