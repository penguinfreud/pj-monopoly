package monopoly.place;

import monopoly.Game;
import monopoly.IPlayer;
import monopoly.util.Consumer0;

public class Trap extends Place {
    static {
        GameMap.registerPlaceReader("Trap", (r, sc) -> new Trap(sc.nextInt()));
    }

    private final double amount;

    private Trap(double amount) {
        super("Trap");
        this.amount = amount;
    }

    @Override
    public void passBy(Game g, Consumer0 cb) {
        IPlayer player = g.getCurrentPlayer();
        player.pay(null, amount, g.format("trap", player.getName(), amount), cb);
    }

    @Override
    public void arriveAt(Game g, Consumer0 cb) {
        passBy(g, cb);
    }
}
