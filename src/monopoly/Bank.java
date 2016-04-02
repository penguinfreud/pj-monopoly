package monopoly;

final class Bank {
    static {
        Game.onGameInit((g, o) ->
            GameCalendar.onMonth.addListener(g, (_g, _o) -> {
                synchronized (_g.lock) {
                    for (AbstractPlayer player: _g.getPlayers()) {
                        player.changeDeposit(_g, player.getDeposit() / 10, "give_interest");
                    }
                }
            }));
    }
}
