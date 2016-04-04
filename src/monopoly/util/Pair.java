package monopoly.util;

public class Pair<A, B> {
    private final A first;
    private final B second;

    public Pair(A a, B b) {
        first = a;
        second = b;
    }

    public final A getFirst() {
        return first;
    }

    public final B getSecond() {
        return second;
    }
}
