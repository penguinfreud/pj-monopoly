package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Game;
import monopoly.util.Consumer0;

class RentFree implements Consumer0 {
    private final Game game;
    private int duration;
    private final AbstractPlayer player;
    private final AbstractPlayer.CardInterface ci;

    RentFree(Game g, AbstractPlayer player, AbstractPlayer.CardInterface ci, int duration) {
        game = g;
        this.player = player;
        this.ci = ci;
        this.duration = duration;
        Game.onTurn.addListener(g, this);
    }

    @Override
    public void run() {
        if (game.getCurrentPlayer() == player) {
            if (duration > 0) {
                ci.setRentFree(player);
                duration--;
                if (duration == 0) {
                    Game.onTurn.removeListener(game, this);
                }
            }
        }
    }
}
