package monopoly;

import java.util.Hashtable;

public class Config {
    protected Hashtable<String, Object> configTable = new Hashtable<>();

    public Config() {
        defaultConfig();
    }

    private void defaultConfig() {
        configTable.put("dice sides", 6);
        configTable.put("init cash", 2000);
        configTable.put("init deposit", 2000);
    }

    void readFile() {

    }
}
