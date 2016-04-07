package monopoly.card;

import monopoly.Card;
import monopoly.Game;
import monopoly.IPlayer;
import monopoly.util.Consumer0;

public class TortoiseCard extends Card {
    static {
        registerCard(new TortoiseCard());
        Game.putDefaultConfig("tortoise-card-price", 3);
        Game.putDefaultConfig("tortoise-card-duration", 3);
    }

    private TortoiseCard() {
        super("TortoiseCard");
    }

    @Override
    protected void use(Game g, Consumer0 cb) {
        Game.onTurn.addListener(g, new Consumer0() {
            private int duration = g.getConfig("tortoise-card-duration");
            private final IPlayer player = g.getCurrentPlayer();

            @Override
            public void run() {
                if (g.getCurrentPlayer() == player) {
                    if (--duration > 0) {
                        g.startWalking(1);
                    } else {
                        Game.onTurn.removeListener(g, this);
                    }
                }
            }
        });
        g.startWalking(1);
    }
}
