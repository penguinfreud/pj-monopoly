package monopoly.async;

public interface Function<S, T> {
    T run(S arg);
}
