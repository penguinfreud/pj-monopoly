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

    protected Bank() {
        super("Bank");
    }

    @Override
    public void onPassingBy(Game g) {
        AbstractPlayer player = g.getCurrentPlayer();
        player.askHowMuchToDepositOrWithdraw(g, (amount) -> {
            player.depositOrWithdraw(g, amount);
        });
    }

    @Override
    public void onLanded(Game g, Callback<Object> cb) {
        onPassingBy(g);
        cb.run(null);
    }
}
