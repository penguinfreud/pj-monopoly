package monopoly.util;

import java.io.Serializable;

public interface Supplier<T> extends Serializable {
    T get();
}
