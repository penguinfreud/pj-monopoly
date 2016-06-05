package monopoly.card;

import monopoly.*;
import monopoly.util.Consumer0;
import monopoly.util.Consumer1;

public class GodOfLandCard extends Card {
    private static final Card instance = new GodOfLandCard();

    static {
        Game.putDefaultConfig("god-of-land-card-price", 15);
        Game.putDefaultConfig("god-of-land-card-duration", 5);
    }

    private GodOfLandCard() {
        super("GodOfLandCard");
    }

    public static void enable(Game g) {
        Cards.enableCard(g, instance);
        Properties.enable(g);
    }

    @Override
    public void use(Game g, Consumer1<Boolean> cb) {
        g.onTurn.addListener(new Consumer0() {
            private int duration = (Integer) g.getConfig("god-of-land-card-duration");
            private final IPlayer player = g.getCurrentPlayer();

            @Override
            public void accept() {
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
        cb.accept(true);
    }
}
