package monopoly;

final class Bank {
    private Bank() {}

    static {
        Game.onInit((g) ->
            GameCalendar.onMonth.addListener(g, () -> {
                synchronized (g.lock) {
                    for (AbstractPlayer player: g.getPlayers()) {
                        player.changeDeposit(g, player.getDeposit() / 10, "give_interest");
                    }
                }
            }));
    }
}
