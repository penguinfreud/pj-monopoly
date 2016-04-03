package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Card;
import monopoly.Game;
import monopoly.util.Callback;

public class GodOfLuckCard extends Card {
    static {
        registerCard(new GodOfLuckCard());
        Game.putDefaultConfig("godofluckcard-price", 13);
        Game.putDefaultConfig("godofluckcard-award", 10000);
    }

    private GodOfLuckCard() {
        super("GodOfLuckCard");
    }

    @Override
    public void use(Game g, AbstractPlayer.CardInterface ci, Callback<Object> cb) {
        AbstractPlayer player = g.getCurrentPlayer();
        ci.changeCash(player, g, g.getConfig("godofluckcard-award"), "blessed_by_god_of_luck");
        Game.onTurn.addListener(g, new Callback<Object>() {
            private int duration = (Integer) g.getConfig("godoflandcard-duration");

            @Override
            public void run(Game g, Object arg) {
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
        });
        cb.run(g, null);
    }
}
