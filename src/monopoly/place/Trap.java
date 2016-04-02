package monopoly.place;

import monopoly.AbstractPlayer;
import monopoly.Game;
import monopoly.Map;
import monopoly.Place;
import monopoly.util.Callback;

public class Trap extends Place {
    static {
        Map.registerPlaceReader("Trap", (r, sc) -> new Trap());
    }

    private Trap() {
        super("Trap");
    }

    @Override
    public void onPassingBy(Game g, AbstractPlayer.PlaceInterface pi, Callback<Object> cb) {
        pi.pay(g.getCurrentPlayer(), g, null, g.getCurrentPlayer().getTotalPossessions() + 1, "trap", cb);
    }

    @Override
    public void onLanded(Game g, AbstractPlayer.PlaceInterface pi, Callback<Object> cb) {
        onPassingBy(g, pi, cb);
    }
}
