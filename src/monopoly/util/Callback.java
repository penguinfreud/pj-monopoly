package monopoly.util;

import monopoly.Game;

import java.io.Serializable;

public interface Callback<T> extends Serializable {
    void run(Game g, T arg);
}
