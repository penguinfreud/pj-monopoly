package monopoly.util;

public class Event3<A, B, C> extends Event<Consumer3<A, B, C>> {
    public final void trigger(A a, B b, C c) {
        synchronized (lock) {
            for (Consumer3<A, B, C> callback : listeners) {
                callback.run(a, b, c);
            }
        }
    }

    public static <A, B, C, T> Event3<A, B, C> New(T t) {
        return new Event3<>();
    }
}
