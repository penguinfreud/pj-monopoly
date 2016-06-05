package monopoly.util;

import java.io.Serializable;

public interface Consumer2<A, B> extends Serializable {
    void accept(A a, B b);
}
