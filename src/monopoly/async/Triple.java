package monopoly.async;

public final class Triple<A, B, C> {
    private final A first;
    private final B second;
    private final C third;

    public Triple(A a, B b, C c) {
        first = a;
        second = b;
        third = c;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

    public C getThird() {
        return third;
    }
}
