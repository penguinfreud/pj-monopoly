package monopoly.event;

import monopoly.Game;

import java.util.concurrent.CopyOnWriteArrayList;

public class Event<T> {
    private CopyOnWriteArrayList<Listener<T>> listeners = new CopyOnWriteArrayList<>();

    public synchronized void addListener(Listener listener) {
        listeners.add(listener);
    }

    public synchronized void trigger(Game g, T arg) {
        for (Listener<T> listener: listeners) {
            listener.run(g, arg);
        }
    }
}
