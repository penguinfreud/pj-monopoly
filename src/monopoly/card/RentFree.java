package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Game;
import monopoly.Properties;
import monopoly.util.Consumer0;

class RentFree implements Consumer0 {
    private int duration;
    private final Game game;
    private final AbstractPlayer player;

    RentFree(AbstractPlayer player, int duration) {
        this.player = player;
        game = player.getGame();
        this.duration = duration;
        Game.onTurn.addListener(game, this);
    }

    @Override
    public void run() {
        if (game.getCurrentPlayer() == player) {
            if (duration > 0) {
                Properties.get(player).setRentFree();
                duration--;
                if (duration == 0) {
                    Game.onTurn.removeListener(game, this);
                }
            }
        }
    }
}
