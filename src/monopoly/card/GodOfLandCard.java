package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Card;
import monopoly.Game;
import monopoly.util.Callback;

public class GodOfLandCard extends Card {
    static {
        registerCard(new GodOfLandCard());
        Game.putDefaultConfig("godoflandcard-price", 15);
        Game.putDefaultConfig("godoflandcard-duration", 5);
    }

    private GodOfLandCard() {
        super("GodOfLandCard");
    }

    @Override
    public void use(Game g, AbstractPlayer.CardInterface ci, Callback<Object> cb) {
        Game.onTurn.addListener(g, new Callback<Object>() {
            private int duration = (Integer) g.getConfig("godoflandcard-duration");
            private final AbstractPlayer player = g.getCurrentPlayer();

            @Override
            public void run(Game g, Object arg) {
                if (g.getCurrentPlayer() == player) {
                    if (duration > 0) {
                        ci.robLand(g);
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
