package monopoly;

import java.util.Hashtable;

public class Config {
    public static class Entry {
        private Object value;

        public boolean getBoolean() {
            return (Boolean)value;
        }

        public int getInt() {
            return (Integer)value;
        }

        public String getString() {
            return (String)value;
        }

        public Entry(boolean v) {
            value = v;
        }

        public Entry(int v) {
            value = v;
        }

        public Entry(String v) {
            value = v;
        }
    }

    private Hashtable<String, Entry> configTable = new Hashtable<>();

    public Config() {
        defaultConfig();
    }

    public Entry get(String key) {
        return configTable.get(key);
    }

    public void put(String key, Entry entry) {
        configTable.put(key, entry);
    }

    private void defaultConfig() {
        put("dice sides", new Entry(6));
        put("init cash", new Entry(2000));
        put("init deposit", new Entry(2000));
    }

    void readFile() {

    }
}
