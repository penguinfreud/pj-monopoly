package monopoly.event;

import monopoly.Game;

public interface Listener<T> {
    public void run(Game g, T arg);
}
