package monopoly.place;

import monopoly.*;
import monopoly.util.Consumer0;

public class Bank extends Place {
    static {
        Map.registerPlaceReader("Bank", (r, sc) -> new Bank());
    }

    private Bank() {
        super("Bank");
    }

    @Override
    public void onPassingBy(Game g, PlaceInterface pi, Consumer0 cb) {
        pi.depositOrWithdraw(cb);
    }

    @Override
    public void onLanded(Game g, PlaceInterface pi, Consumer0 cb) {
        onPassingBy(g, pi, cb);
    }
}
