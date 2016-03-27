package monopoly.event;

public abstract class Function<S, T> {
    public abstract T run(S arg);
}
