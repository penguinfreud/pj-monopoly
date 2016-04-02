package monopoly.async;

public final class Tuple<A, B> {
    private final A first;
    private final B second;

    public Tuple(A a, B b) {
        first = a;
        second = b;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }
}
