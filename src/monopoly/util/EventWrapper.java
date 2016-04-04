package monopoly.util;

public class EventWrapper<H extends Host, T> {
    private final Parasite<H, ? extends Event<T>> event;

    public EventWrapper(Parasite<H, ? extends Event<T>> event) {
        this.event = event;
    }

    public void addListener(H host, T listener) {
        event.get(host).addListener(listener);
    }

    public void removeListener(H host, T listener) {
        event.get(host).removeListener(listener);
    }
}
