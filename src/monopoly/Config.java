package monopoly;

import java.io.Serializable;
import java.util.Hashtable;

public class Config implements Serializable {
    private final Hashtable<String, Object> configTable = new Hashtable<>();
    private Config base;

    Config() {
        this(null);
    }

    Config(Config base) {
        this.base = base;
    }

    Config getBase() {
        return base;
    }

    void setBase(Config _base) {
        if (base == null) {
            base = _base;
        } else {
            base.setBase(_base);
        }
    }

    Object get(String key) {
        if (configTable.containsKey(key)) {
            return configTable.get(key);
        } else if (base != null) {
            return base.get(key);
        } else {
            return null;
        }
    }

    void put(String key, Object value) {
        configTable.put(key, value);
    }

    void remove(String key) {
        configTable.remove(key);
    }

    void clear() {
        configTable.clear();
    }
}
