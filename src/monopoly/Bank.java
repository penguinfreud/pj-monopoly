package monopoly;

final class Bank {
    private Bank() {}

    static {
        Game.onInit(g -> GameCalendar.onMonth.addListener(g, () -> {
                synchronized (g.lock) {
                    for (AbstractPlayer player: g.getPlayers()) {
                        int amount = player.getDeposit() / 10;
                        String msg = g.format("give_interest", player.getName(), amount);
                        player.changeDeposit(amount, msg);
                    }
                }
            }));
    }
}
