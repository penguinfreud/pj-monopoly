package monopoly.event;

import monopoly.Game;

import java.io.Serializable;

public interface Listener<T> extends Serializable {
    void run(Game g, T arg);
}
