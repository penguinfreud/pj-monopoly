package monopoly.place;

import monopoly.AbstractPlayer;
import monopoly.Game;
import monopoly.Map;
import monopoly.Place;
import monopoly.async.Callback;

public class Bank extends Place {
    static {
        Map.registerPlaceReader("Bank", (r, sc) -> new Bank());
    }

    private Bank() {
        super("Bank");
    }

    @Override
    public void onPassingBy(Game g, AbstractPlayer.PlaceInterface pi, Callback<Object> cb) {
        pi.depositOrWithdraw(g, cb);
    }

    @Override
    public void onLanded(Game g, AbstractPlayer.PlaceInterface pi, Callback<Object> cb) {
        onPassingBy(g, pi, cb);
    }
}
