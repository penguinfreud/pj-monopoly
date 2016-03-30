package monopoly;

import monopoly.async.Callback;

public class StopTheGame extends Property {
    static {
        Map.registerPlaceReader("StopTheGame", (r, sc) -> new StopTheGame());
    }

    public StopTheGame() {
        super("StopTheGame", Integer.MAX_VALUE);
    }

    @Override
    public int getRent() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void onPassingBy(Game g) {
        g.getCurrentPlayer().payRent(g);
    }

    @Override
    public void onLanded(Game g, Callback<Object> cb) {
        g.getCurrentPlayer().payRent(g);
        cb.run(null);
    }
}
