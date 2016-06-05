package monopoly.card;

import monopoly.Game;
import monopoly.IPlayer;
import monopoly.Properties;
import monopoly.util.Consumer0;

class RentFree implements Consumer0 {
    private int duration;
    private final Game game;
    private final IPlayer player;

    RentFree(IPlayer player, int duration) {
        this.player = player;
        game = player.getGame();
        this.duration = duration;
        game.onTurn.addListener(this);
    }

    @Override
    public void accept() {
        if (game.getCurrentPlayer() == player) {
            if (duration > 0) {
                Properties.get(player).setRentFree();
                duration--;
                if (duration == 0) {
                    game.onTurn.removeListener(this);
                }
            }
        }
    }
}
