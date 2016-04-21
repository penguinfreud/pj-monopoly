package monopoly.card;

import monopoly.Card;
import monopoly.Game;
import monopoly.IPlayer;
import monopoly.Cards;
import monopoly.place.Place;
import monopoly.util.Consumer1;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RobCard extends Card {
    private static final Card instance = new RobCard();
    static {
        Game.putDefaultConfig("rob-card-price", 7);
        Game.putDefaultConfig("rob-card-reach", 5);
    }

    private RobCard() {
        super("RobCard");
    }

    public static void enable(Game g) {
        Cards.enableCard(g, instance);
    }

    @Override
    public void use(Game g, Consumer1<Boolean> cb) {
        IPlayer current = g.getCurrentPlayer();
        ((Cards.IPlayerWithCards) current).askForTargetPlayer(getName(), player -> {
            synchronized (g.lock) {
                int reach = g.getConfig("rob-card-reach");
                if (player != null &&
                        Place.withinReach(current.getCurrentPlace(), player.getCurrentPlace(), reach) >= 0) {
                    List<Card> cards = Cards.get(player).getCards();
                    if (!cards.isEmpty()) {
                        Card card = cards.get(ThreadLocalRandom.current().nextInt(cards.size()));
                        Cards.get(player).removeCard(card);
                        Cards.get(current).addCard(card);
                        cb.run(true);
                    } else {
                        cb.run(false);
                    }
                } else {
                    cb.run(false);
                }
            }
        });
    }
}
