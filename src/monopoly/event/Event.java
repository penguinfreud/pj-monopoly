package monopoly.event;

import java.util.ArrayList;

public class Event<T> {
    private ArrayList<Listener<T>> listeners;

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void trigger(T arg) {
        for (Listener<T> listener: listeners) {
            listener.run(arg);
        }
    }
}
