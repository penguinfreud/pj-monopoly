package monopoly.ui;

import monopoly.Game;
import monopoly.Map;
import monopoly.Place;
import monopoly.Property;

import java.awt.Graphics;

public class DrawablePlace extends Place {
    private Place place;
    private int x, y;
    
    public DrawablePlace(int x, int y, Place place) {
        super(place.getName());
        this.x = x;
        this.y = y;
        this.place = place;
    }

    public String getName() {
        return place.getName();
    }

    public void onLanded(Game g) {
        place.onLanded(g);
    }

    public void onPassingBy(Game g) {
        place.onPassingBy(g);
    }
    
    public Property asProperty() {
        return place.asProperty();
    }
    
    public void draw(Graphics g) {
    
    }
    
    static {
        Map.registerPlaceType("Drawable", (sc) -> new DrawablePlace(sc.nextInt(), sc.nextInt(), Map.readPlace(sc)));
    }
}
