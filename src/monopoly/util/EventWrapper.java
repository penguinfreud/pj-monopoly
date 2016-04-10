package monopoly.util;

public class EventWrapper<T> {
    private final Event<T> event;

    public EventWrapper(Event<T> event) {
        this.event = event;
    }

    public void addListener(T listener) {
        event.addListener(listener);
    }

    public void removeListener(T listener) {
        event.removeListener(listener);
    }
}
