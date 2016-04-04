package monopoly.place;

import monopoly.AbstractPlayer;
import monopoly.Game;
import monopoly.Map;
import monopoly.Place;
import monopoly.util.Consumer0;

public class Bank extends Place {
    static {
        Map.registerPlaceReader("Bank", (r, sc) -> new Bank());
        Game.putDefaultConfig("bank-max-transfer", 100000);
    }

    private Bank() {
        super("Bank");
    }

    @Override
    public void onPassingBy(Game g, AbstractPlayer.PlaceInterface pi, Consumer0 cb) {
        pi.depositOrWithdraw(cb);
    }

    @Override
    public void onLanded(Game g, AbstractPlayer.PlaceInterface pi, Consumer0 cb) {
        onPassingBy(g, pi, cb);
    }
}
