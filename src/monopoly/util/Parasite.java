package monopoly.util;

import java.io.Serializable;

public class Parasite<H extends Host, T> {
    private final Serializable key;

    public Parasite(Serializable key, Consumer1<Consumer1<H>> onInit, Function1<H, T> factory) {
        this.key = key;
        onInit.run((host) -> host.setParasite(key, factory.run(host)));
    }

    public T get(H host) {
        return host.getParasite(key);
    }
}
