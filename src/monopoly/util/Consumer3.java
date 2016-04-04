package monopoly.util;

import java.io.Serializable;

public interface Consumer3<A, B, C> extends Serializable {
    void run(A a, B b, C c);
}
