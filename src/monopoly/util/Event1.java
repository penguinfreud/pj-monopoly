package monopoly.util;

public class Event1<A> extends Event<Consumer1<A>> {
    public void trigger(A a) {
        synchronized (lock) {
            for (Consumer1<A> callback : listeners) {
                callback.accept(a);
            }
        }
    }

    public static <A, T> Event1<A> New(T t) {
        return new Event1<>();
    }
}
