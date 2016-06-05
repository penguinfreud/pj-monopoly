package monopoly.util;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InitEvent<T> extends Event1<T> {
    private List<WeakReference<T>> memory = new CopyOnWriteArrayList<>();

    @Override
    public void addListener(Consumer1<T> listener) {
        synchronized (lock) {
            for (int i = memory.size() - 1; i >= 0; i--) {
                T t = memory.get(i).get();
                if (t == null) {
                    memory.remove(i);
                } else {
                    listener.accept(t);
                }
            }
            listeners.add(listener);
        }
    }

    @Override
    public void removeListener(Consumer1<T> listener) {
        throw new Error("not supported");
    }

    @Override
    public void trigger(T t) {
        synchronized (lock) {
            memory.add(new WeakReference<T>(t));
            super.trigger(t);
        }
    }
}
