package monopoly.event;

public interface Listener<T> {
    public void run(T arg);
}
