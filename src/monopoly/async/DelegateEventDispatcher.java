package monopoly.async;

public class DelegateEventDispatcher<T> {
    private final EventDispatcher<T> eventDispatcher;

    public DelegateEventDispatcher(EventDispatcher<T> eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    public void addListener(Callback<T> listener) {
        eventDispatcher.addListener(listener);
    }

    public void removeListener(Callback<T> listener) {
        eventDispatcher.removeListener(listener);
    }
}
