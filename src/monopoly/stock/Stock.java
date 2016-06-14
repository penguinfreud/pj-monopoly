package monopoly.stock;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import monopoly.Game;
import monopoly.GameObject;

public class Stock implements GameObject {
    private final StringProperty name = new SimpleStringProperty();

    public Stock(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    @Override
    public String toString(Game g) {
        return g.getText(name.get());
    }
}
