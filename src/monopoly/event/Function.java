package monopoly.event;

public interface Function<S, T> {
    T run(S arg) throws Exception;
}
