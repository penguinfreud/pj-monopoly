package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Game;
import monopoly.Place;
import monopoly.util.Consumer0;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RobCard extends Card {
    static {
        registerCard(new RobCard());
        Game.putDefaultConfig("rob-card-price", 7);
        Game.putDefaultConfig("rob-card-reach", 5);
    }

    private RobCard() {
        super("RobCard");
    }

    @Override
    public void use(Game g, AbstractPlayer.CardInterface ci, Consumer0 cb) {
        AbstractPlayer current = g.getCurrentPlayer();
        current.askForPlayer(g, getName(), (player) -> {
            int reach = g.getConfig("rob-card-reach");
            if (player != null &&
                    Place.withinReach(current.getCurrentPlace(), player.getCurrentPlace(), reach) >= 0) {
                List<Card> cards = player.getCards();
                if (!cards.isEmpty()) {
                    Card card = cards.get(ThreadLocalRandom.current().nextInt(cards.size()));
                    ci.removeCard(player, card);
                    ci.addCard(current, card);
                }
            }
            cb.run();
        });
    }
}
