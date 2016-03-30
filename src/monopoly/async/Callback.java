package monopoly.async;

import monopoly.Game;

import java.io.Serializable;

public interface Callback<T> extends Serializable {
    void run(T arg);
}
