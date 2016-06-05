package monopoly.card;

import monopoly.Card;
import monopoly.Cards;
import monopoly.Game;
import monopoly.IPlayer;
import monopoly.util.Consumer0;
import monopoly.util.Consumer1;

public class TortoiseCard extends Card {
    private static final Card instance = new TortoiseCard();

    static {
        Game.putDefaultConfig("tortoise-card-price", 3);
        Game.putDefaultConfig("tortoise-card-duration", 3);
    }

    private TortoiseCard() {
        super("TortoiseCard");
    }

    public static void enable(Game g) {
        Cards.enableCard(g, instance);
    }

    @Override
    protected void use(Game g, Consumer1<Boolean> cb) {
        g.onTurn.addListener(new Consumer0() {
            private int duration = g.getConfig("tortoise-card-duration");
            private final IPlayer player = g.getCurrentPlayer();

            @Override
            public void accept() {
                if (g.getCurrentPlayer() == player) {
                    if (--duration > 0) {
                        g.setDice(1);
                    } else {
                        g.onTurn.removeListener(this);
                    }
                }
            }
        });
        g.setDice(1);
        cb.accept(true);
    }
}
