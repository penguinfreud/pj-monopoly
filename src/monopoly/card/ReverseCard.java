package monopoly.card;

import monopoly.Card;
import monopoly.Game;
import monopoly.IPlayer;
import monopoly.Cards;
import monopoly.place.Place;
import monopoly.util.Consumer1;

public class ReverseCard extends Card {
    private static final Card instance = new ReverseCard();
    static {
        Game.putDefaultConfig("reverse-card-price", 3);
        Game.putDefaultConfig("reverse-card-reach", 5);
    }

    private ReverseCard() {
        super("ReverseCard");
    }

    public static void enable(Game g) {
        Cards.enableCard(g, instance);
    }

    @Override
    public void use(Game g, Consumer1<Boolean> cb) {
        IPlayer current = g.getCurrentPlayer();
        ((Cards.IPlayerWithCards) current).askForTargetPlayer(getName(), player -> {
            synchronized (g.lock) {
                int reach = g.getConfig("reverse-card-reach");
                if (player != null &&
                        Place.withinReach(current.getCurrentPlace(), player.getCurrentPlace(), reach) >= 0) {
                    player.reverse();
                    cb.run(true);
                } else {
                    cb.run(false);
                }
            }
        });
    }
}
