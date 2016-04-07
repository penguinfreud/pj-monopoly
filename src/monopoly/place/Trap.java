package monopoly.place;

import monopoly.*;
import monopoly.util.Consumer0;

public class Trap extends Place {
    static {
        GameMap.registerPlaceReader("Trap", (r, sc) -> new Trap());
    }

    private Trap() {
        super("Trap");
    }

    @Override
    public void passBy(Game g, Consumer0 cb) {
        AbstractPlayer player = g.getCurrentPlayer();
        player.pay(null, player.getTotalPossessions() + 1, g.format("trap", player.getName()), cb);
    }

    @Override
    public void arriveAt(Game g, Consumer0 cb) {
        passBy(g, cb);
    }
}
