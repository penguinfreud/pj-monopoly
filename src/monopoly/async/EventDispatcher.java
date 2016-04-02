package monopoly.async;

import monopoly.Game;

import java.io.Serializable;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EventDispatcher<T> implements Serializable {
    private final CopyOnWriteArrayList<Callback<T>> listeners = new CopyOnWriteArrayList<>();

    public synchronized void addListener(Callback<T> listener) {
        listeners.add(listener);
    }

    public synchronized void removeListener(Callback<T> listener) {
        listeners.remove(listener);
    }

    public synchronized void trigger(Game g, T arg) {
        for (Callback<T> callback : listeners) {
            callback.run(g, arg);
        }
    }
}
