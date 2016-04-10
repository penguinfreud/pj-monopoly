package monopoly.util;

import java.io.Serializable;

public class Parasite<H extends Host, T> {
    private final Serializable key;

    public Parasite(Serializable key) {
        this.key = key;
    }

    public T get(H host) {
        return host.getParasite(key);
    }

    public void set(H host, T t) {
        host.setParasite(key, t);
    }
}
