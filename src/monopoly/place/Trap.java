package monopoly.place;

import monopoly.*;
import monopoly.util.Consumer0;

public class Trap extends Place {
    static {
        Map.registerPlaceReader("Trap", (r, sc) -> new Trap());
    }

    private Trap() {
        super("Trap");
    }

    @Override
    public void onPassingBy(Game g, PlaceInterface pi, Consumer0 cb) {
        pi.pay(g.getCurrentPlayer(), null, g.getCurrentPlayer().getTotalPossessions() + 1, "trap", cb);
    }

    @Override
    public void onLanded(Game g, PlaceInterface pi, Consumer0 cb) {
        onPassingBy(g, pi, cb);
    }
}
