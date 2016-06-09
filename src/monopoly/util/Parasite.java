package monopoly.util;

public class Parasite<H extends Host, T> {
    private final Object key = new Object();

    public T get(H host) {
        return host.getParasite(key);
    }

    public void set(H host, T t) {
        host.setParasite(key, t);
    }
}
