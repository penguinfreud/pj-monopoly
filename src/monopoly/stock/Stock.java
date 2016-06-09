package monopoly.stock;

import monopoly.Game;
import monopoly.GameObject;

public class Stock implements GameObject {
    private final String name;

    public Stock(String name) {
        this.name = name;
    }

    @Override
    public String toString(Game g) {
        return g.getText(name);
    }
}
