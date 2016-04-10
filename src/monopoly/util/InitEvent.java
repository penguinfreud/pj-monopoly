package monopoly.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InitEvent<T> extends Event1<T> {
    private static final class SerializableReference<S> implements Serializable {
        private transient WeakReference<S> ref;

        SerializableReference(S referent) {
            ref = new WeakReference<>(referent);
        }

        S get() {
            return ref.get();
        }

        @SuppressWarnings("unchecked")
        private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
            ref = new WeakReference<>((S) ois.readObject());
        }

        private void writeObject(ObjectOutputStream oos) throws IOException {
            oos.writeObject(ref.get());
        }
    }

    private List<SerializableReference<T>> memory = new CopyOnWriteArrayList<>();

    @Override
    public void addListener(Consumer1<T> listener) {
        synchronized (lock) {
            for (int i = memory.size() - 1; i>=0; i--) {
                T t = memory.get(i).get();
                if (t == null) {
                    memory.remove(i);
                } else {
                    listener.run(t);
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
            memory.add(new SerializableReference<T>(t));
            super.trigger(t);
        }
    }
}
