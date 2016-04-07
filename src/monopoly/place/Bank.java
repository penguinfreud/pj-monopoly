package monopoly.place;

import monopoly.*;
import monopoly.util.Consumer0;

public class Bank extends Place {
    static {
        GameMap.registerPlaceReader("Bank", (r, sc) -> new Bank());
    }

    private Bank() {
        super("Bank");
    }

    @Override
    protected void passBy(Game g, Consumer0 cb) {
        g.getCurrentPlayer().depositOrWithdraw(cb);
    }

    @Override
    protected void arriveAt(Game g, Consumer0 cb) {
        passBy(g, cb);
    }
}
