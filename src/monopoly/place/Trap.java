package monopoly.place;

import monopoly.*;
import monopoly.util.Consumer0;

public class Trap extends Place {
    static {
        GameMap.registerPlaceReader("Trap", (r, sc) -> new Trap(sc.nextInt()));
    }

    private final int amount;

    private Trap(int amount) {
        super("Trap");
        this.amount = amount;
    }

    @Override
    protected void passBy(Game g, Consumer0 cb) {
        IPlayer player = g.getCurrentPlayer();
        player.pay(null, amount, g.format("trap", player.getName(), amount), cb);
    }

    @Override
    protected void arriveAt(Game g, Consumer0 cb) {
        passBy(g, cb);
    }
}
