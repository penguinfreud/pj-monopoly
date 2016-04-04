package monopoly.util;

public class Parasite<H extends Host, T> {
    private final SerializableObject key = new SerializableObject();

    public Parasite(Consumer1<Consumer1<H>> onInit, Function1<H, T> factory) {
        onInit.run((host) -> host.setParasite(key, factory.run(host)));
    }

    public T get(H host) {
        return host.getParasite(key);
    }
}
