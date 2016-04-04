package monopoly.util;

import java.io.Serializable;

public interface Consumer2<A, B> extends Serializable {
    void run(A a, B b);
}
