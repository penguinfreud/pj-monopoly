package monopoly.util;

import monopoly.Game;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Event<T> {
    private final SerializableObject key = new SerializableObject();

    public Event() {
        Game.onGameInit((g, o) -> g.store(key, new CopyOnWriteArrayList<>()));
    }

    private List<Callback<T>> getListeners(Game g) {
        return g.getStorage(key);
    }

    public final void addListener(Game g, Callback<T> listener) {
        getListeners(g).add(listener);
    }

    public final void removeListener(Game g, Callback<T> listener) {
        getListeners(g).remove(listener);
    }

    public final void trigger(Game g, T arg) {
        for (Callback<T> callback : getListeners(g)) {
            callback.run(g, arg);
        }
    }
}
