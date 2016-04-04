package monopoly.util;

public class Triple<A, B, C> extends Pair<A, B> {
    private final C third;

    public Triple(A a, B b, C c) {
        super(a, b);
        third = c;
    }

    public final C getThird() {
        return third;
    }
}
