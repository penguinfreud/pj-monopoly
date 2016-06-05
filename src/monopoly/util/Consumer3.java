package monopoly.util;

import java.io.Serializable;

public interface Consumer3<A, B, C> extends Serializable {
    void accept(A a, B b, C c);
}
