package monopoly.event;

import monopoly.Game;

import java.io.Serializable;
import java.util.concurrent.CopyOnWriteArrayList;

public class Event<T> implements Serializable {
    private CopyOnWriteArrayList<Listener<T>> listeners = new CopyOnWriteArrayList<>();

    public synchronized void addListener(Listener listener) {
        listeners.add(listener);
    }

    public synchronized void trigger(T arg) {
        for (Listener<T> listener: listeners) {
            listener.run(arg);
        }
    }
}
