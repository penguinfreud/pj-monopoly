package monopoly.util;

public class Event0 extends Event<Consumer0> {
    public final void trigger() {
        synchronized (lock) {
            listeners.stream().forEach(Consumer0::run);
        }
    }

    public static <T> Event0 New(T t) {
        return new Event0();
    }
}
