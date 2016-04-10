package monopoly.card;

import monopoly.Card;
import monopoly.Game;
import monopoly.IPlayer;
import monopoly.Properties;
import monopoly.util.Consumer0;
import monopoly.util.Consumer1;

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
    public void use(Game g, Consumer1<Boolean> cb) {
        g.onTurn.addListener(new Consumer0() {
            private int duration = (Integer) g.getConfig("god-of-land-card-duration");
            private final IPlayer player = g.getCurrentPlayer();

            @Override
            public void run() {
                if (g.getCurrentPlayer() == player) {
                    if (duration > 0) {
                        Properties.get(player).robLand(player.getCurrentPlace().asProperty());
                        duration--;
                        if (duration == 0) {
                            g.onTurn.removeListener(this);
                        }
                    }
                }
            }
        });
        cb.run(true);
    }
}
