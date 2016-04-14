package monopoly.extension;

import monopoly.Game;
import monopoly.IPlayer;

public final class BankSystem {
    private BankSystem() {}

    public static void init(Game g) {
        GameCalendar.init(g);
        GameCalendar.onMonth.get(g).addListener(() -> {
            synchronized (g.lock) {
                for (IPlayer player : g.getPlayers()) {
                    int amount = player.getDeposit() / 10;
                    String msg = g.format("give_interest", player.getName(), amount);
                    player.changeDeposit(amount, msg);
                }
            }
        });
    }
}
