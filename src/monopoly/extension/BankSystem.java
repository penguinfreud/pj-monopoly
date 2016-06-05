package monopoly.extension;

import monopoly.Game;
import monopoly.IPlayer;
import monopoly.util.Parasite;

public final class BankSystem {
    private BankSystem() {
    }

    private static final Parasite<Game, Boolean> inited = new Parasite<>("BankSystem");

    public static void enable(Game g) {
        if (inited.get(g) == null) {
            inited.set(g, true);
            GameCalendar.enable(g);
            GameCalendar.onMonth.get(g).addListener(() -> {
                synchronized (g.lock) {
                    for (IPlayer player : g.getPlayers()) {
                        double amount = player.getDeposit() / 10;
                        String msg = g.format("give_interest", player.getName(), amount);
                        player.changeDeposit(amount, msg);
                    }
                }
            });
        }
    }

    public static boolean isEnabled(Game g) {
        return inited.get(g) != null;
    }
}
