package monopoly.util;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

abstract class Event<T> implements Serializable {
    final SerializableObject lock = new SerializableObject();
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
