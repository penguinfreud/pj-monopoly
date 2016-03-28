package monopoly.ui;

import monopoly.*;

import java.awt.Graphics;

public class UIPlace extends Place {
    private Place place;
    private int x, y;
    
    public UIPlace(int x, int y, Place place) {
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
    public void onLanded(Game g) {
        place.onLanded(g);
    }

    @Override
    public void onPassingBy(Game g) {
        place.onPassingBy(g);
    }

    @Override
    public Property asProperty() {
        return place.asProperty();
    }
    
    public void draw(Graphics g) {
    
    }
}
