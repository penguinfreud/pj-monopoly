package monopoly.stock;

import monopoly.Game;
import monopoly.GameObject;

import java.io.Serializable;

public class Stock implements GameObject, Serializable {
    private final String name;

    public Stock(String name) {
        this.name = name;
    }

    @Override
    public String toString(Game g) {
        return g.getText(name);
    }
}
