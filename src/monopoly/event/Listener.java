package monopoly.event;

import java.io.Serializable;

public interface Listener<T> extends Serializable {
    void run(T arg);
}
