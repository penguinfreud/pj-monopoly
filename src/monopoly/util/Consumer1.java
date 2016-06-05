package monopoly.util;

import java.io.Serializable;

public interface Consumer1<A> extends Serializable {
    void accept(A a);
}
