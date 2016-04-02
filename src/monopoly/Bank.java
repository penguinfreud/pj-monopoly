package monopoly;

final class Bank {
    static {
        GameCalendar.onMonth.addListener((g, o) -> {
            synchronized (g.lock) {
                for (AbstractPlayer player : g.getPlayers()) {
                    player.changeDeposit(g, player.getDeposit() / 10, "");
                }
            }
        });
    }
}
