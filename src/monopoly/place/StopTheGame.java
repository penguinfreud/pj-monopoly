package monopoly.place;

import monopoly.Game;
import monopoly.Map;
import monopoly.Place;
import monopoly.async.Callback;

public class StopTheGame extends Place {
    static {
        Map.registerPlaceReader("StopTheGame", (r, sc) -> new StopTheGame());
    }

    public StopTheGame() {
        super("StopTheGame");
    }

    @Override
    public void onPassingBy(Game g) {
        g.getCurrentPlayer().giveUp(g);
    }

    @Override
    public void onLanded(Game g, Callback<Object> cb) {
        onPassingBy(g);
        cb.run(null);
    }
}
