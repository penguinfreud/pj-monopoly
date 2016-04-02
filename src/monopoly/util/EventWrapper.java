package monopoly.util;

import monopoly.Game;

public class EventWrapper<T> {
    private final Event<T> event;

    public EventWrapper(Event<T> event) {
        this.event = event;
    }

    public void addListener(Game g, Callback<T> listener) {
        event.addListener(g, listener);
    }

    public void removeListener(Game g, Callback<T> listener) {
        event.removeListener(g, listener);
    }
}
