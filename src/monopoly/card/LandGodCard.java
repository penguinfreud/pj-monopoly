package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Card;
import monopoly.Game;
import monopoly.util.Callback;

public class LandGodCard extends Card {
    static {
        Card.registerCard(new LandGodCard());
        Game.putDefaultConfig("landgodcard-price", 15);
        Game.putDefaultConfig("landgodcard-duration", 5);
    }

    private LandGodCard() {
        super("LandGodCard");
    }

    @Override
    public void use(Game g, AbstractPlayer.CardInterface ci, Callback<Object> cb) {
        g.onTurn.addListener(g, new Callback<Object>() {
            private int duration = (Integer) g.getConfig("landgodcard-duration");
            private final AbstractPlayer player = g.getCurrentPlayer();

            @Override
            public void run(Game g, Object arg) {
                if (g.getCurrentPlayer() == player) {
                    if (duration > 0) {
                        ci.robLand(g);
                        duration--;
                        if (duration == 0) {
                            g.onTurn.removeListener(g, this);
                        }
                    }
                }
            }
        });
    }
}
