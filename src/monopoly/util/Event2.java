package monopoly.util;

public class Event2<A, B> extends Event<Consumer2<A, B>> {
    public final void trigger(A a, B b) {
        synchronized (lock) {
            for (Consumer2<A, B> callback : listeners) {
                callback.run(a, b);
            }
        }
    }

    public static <A, B, T> Event2<A, B> New(T t) {
        return new Event2<>();
    }
}
