package monopoly.place;

import monopoly.AbstractPlayer;
import monopoly.Game;
import monopoly.Map;
import monopoly.Place;
import monopoly.async.Callback;

public class StopTheGame extends Place {
    static {
        Map.registerPlaceReader("StopTheGame", (r, sc) -> new StopTheGame());
    }

    private StopTheGame() {
        super("StopTheGame");
    }

    @Override
    public void onPassingBy(Game g, AbstractPlayer.PlaceInterface pi, Callback<Object> cb) {
        pi.pay(g.getCurrentPlayer(), g, null, g.getCurrentPlayer().getTotalPossessions() + 1, "", cb);
    }

    @Override
    public void onLanded(Game g, AbstractPlayer.PlaceInterface pi, Callback<Object> cb) {
        onPassingBy(g, pi, cb);
    }
}
