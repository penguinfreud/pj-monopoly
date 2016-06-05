package monopoly.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

abstract class Event<T> {
    final Object lock = new Object();
    final List<T> listeners = new CopyOnWriteArrayList<>();

    public void addListener(T listener) {
        synchronized (lock) {
            listeners.add(listener);
        }
    }

    public void removeListener(T listener) {
        synchronized (lock) {
            listeners.remove(listener);
        }
    }

    public void clearListeners() {
        synchronized (lock) {
            listeners.clear();
        }
    }
}
