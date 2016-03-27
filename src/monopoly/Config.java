package monopoly;

import java.util.Hashtable;

public class Config {
    public static class Entry {
        private boolean booleanVaue;
        private int intValue;
        private String stringValue;

        public boolean getBoolean() {
            return booleanVaue;
        }

        public int getInt() {
            return intValue;
        }

        public String getString() {
            return stringValue;
        }

        public Entry(boolean v) {
            booleanVaue = v;
        }

        public Entry(int v) {
            intValue = v;
        }

        public Entry(String v) {
            stringValue = v;
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
        put("dice-sides", new Entry(6));
    }
}
