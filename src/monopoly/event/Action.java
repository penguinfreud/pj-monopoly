package monopoly.event;

public abstract class Action<T> implements Listener<T> {
    public abstract void run(T arg);
}
