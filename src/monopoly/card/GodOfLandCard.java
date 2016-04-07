package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Card;
import monopoly.Game;
import monopoly.Properties;
import monopoly.util.Consumer0;

public class GodOfLandCard extends Card {
    static {
        registerCard(new GodOfLandCard());
        Game.putDefaultConfig("god-of-land-card-price", 15);
        Game.putDefaultConfig("god-of-land-card-duration", 5);
    }

    private GodOfLandCard() {
        super("GodOfLandCard");
    }

    @Override
    public void use(Game g, Consumer0 cb) {
        Game.onTurn.addListener(g, new Consumer0() {
            private int duration = (Integer) g.getConfig("god-of-land-card-duration");
            private final AbstractPlayer player = g.getCurrentPlayer();

            @Override
            public void run() {
                if (g.getCurrentPlayer() == player) {
                    if (duration > 0) {
                        Properties.get(player).robLand();
                        duration--;
                        if (duration == 0) {
                            Game.onTurn.removeListener(g, this);
                        }
                    }
                }
            }
        });
        cb.run();
    }
}
