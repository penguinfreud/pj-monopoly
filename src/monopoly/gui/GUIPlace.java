package monopoly.gui;

import monopoly.*;
import monopoly.DelegatePlace;

import java.awt.Graphics;

public class GUIPlace extends DelegatePlace {
    static {
        GameMap.registerPlaceReader("GUIPlace", (r, sc) -> new GUIPlace(sc.nextInt(), sc.nextInt(), r.readPlace(sc)));
    }

    private final int x, y;

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
