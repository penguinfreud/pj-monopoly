package monopoly.gui;

import monopoly.*;
import monopoly.async.Callback;
import monopoly.place.DelegatePlace;

import java.awt.Graphics;

public class GUIPlace extends DelegatePlace {
    static {
        Map.registerPlaceReader("GUIPlace", (r, sc) -> new GUIPlace(sc.nextInt(), sc.nextInt(), r.readPlace(sc)));
    }

    private int x, y;

    public GUIPlace(int x, int y, Place place) {
        super(place);
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    
    public void draw(Graphics g) {
    
    }
}
