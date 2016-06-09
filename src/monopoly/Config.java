package monopoly;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.util.Hashtable;

public class Config {
    private final ObservableMap<String, Object> configTable = FXCollections.observableMap(new Hashtable<>());
    private Config base;

    public Config() {
        this(null);
    }

    public Config(Config base) {
        this.base = base;
    }

    public Config getBase() {
        return base;
    }

    public void setBase(Config _base) {
        if (base == null) {
            base = _base;
        } else {
            base.setBase(_base);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        if (configTable.containsKey(key)) {
            return (T) configTable.get(key);
        } else if (base != null) {
            return base.get(key);
        } else {
            return null;
        }
    }

    public void put(String key, Object value) {
        configTable.put(key, value);
    }

    public StringBinding stringValueAt(String key) {
        return Bindings.createStringBinding(() -> (String) get(key), configTable);
    }

    public IntegerBinding integerValueAt(String key) {
        return Bindings.createIntegerBinding(() -> (Integer) get(key), configTable);
    }
}
