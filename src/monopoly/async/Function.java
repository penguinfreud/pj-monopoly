package monopoly.async;

import monopoly.Game;

public interface Function<S, T> {
    T run(Game g, S arg);
}
