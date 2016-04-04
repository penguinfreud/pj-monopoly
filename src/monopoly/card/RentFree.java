package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Game;
import monopoly.util.Callback;

class RentFree implements Callback<Object> {
    private int duration;
    private AbstractPlayer player;
    private AbstractPlayer.CardInterface ci;

    RentFree(Game g, AbstractPlayer player, AbstractPlayer.CardInterface ci, int duration) {
        this.player = player;
        this.ci = ci;
        this.duration = duration;
        Game.onTurn.addListener(g, this);
    }

    @Override
    public void run(Game g, Object o) {
        if (g.getCurrentPlayer() == player) {
            if (duration > 0) {
                ci.setRentFree(player, g);
                duration--;
                if (duration == 0) {
                    Game.onTurn.removeListener(g, this);
                }
            }
        }
    }
}
