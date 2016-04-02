package monopoly.async;

import monopoly.Game;

import java.io.Serializable;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventDispatcher<T> implements Serializable {
    private final CopyOnWriteArrayList<Callback<T>> listeners = new CopyOnWriteArrayList<>();

    public synchronized void addListener(Callback<T> callback) {
        listeners.add(callback);
    }

    public synchronized void trigger(Game g, T arg) {
        for (Callback<T> callback : listeners) {
            callback.run(g, arg);
        }
    }
}
