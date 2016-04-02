package monopoly;

import java.io.Serializable;

public class Bank implements Serializable {
    public Bank(Game g) {
        g.onO("month", (_g, o) -> {
            synchronized (_g.lock) {
                for (AbstractPlayer player : _g.getPlayers()) {
                    giveInterest(_g, player);
                }
            }
        });
    }

    private void giveInterest(Game g, AbstractPlayer player) {
        player.changeDeposit(g, player.getDeposit() / 10, "");
    }
}
