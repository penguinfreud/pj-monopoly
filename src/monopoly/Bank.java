package monopoly;

import java.io.Serializable;

public class Bank implements Serializable {
    public Bank(Game g) {
        g.onO("month", (o) -> {
            for (AbstractPlayer player: g.getPlayers()) {
                giveInterest(g, player);
            }
        });
    }

    private void giveInterest(Game g, AbstractPlayer player) {
        player.changeDeposit(g, player.getDeposit() / 10, "");
    }
}
