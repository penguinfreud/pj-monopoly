package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Card;
import monopoly.Game;
import monopoly.Place;
import monopoly.util.Callback;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RobCard extends Card {
    static {
        registerCard(new RobCard());
        Game.putDefaultConfig("robcard-price", 7);
        Game.putDefaultConfig("robcard-reach", 5);
    }

    private RobCard() {
        super("RobCard");
    }

    @Override
    public void use(Game g, AbstractPlayer.CardInterface ci, Callback<Object> cb) {
        AbstractPlayer current = g.getCurrentPlayer();
        current.askForPlayer(g, getName(), (_g, player) -> {
            int reach = _g.getConfig("robcard-reach");
            if (player != null &&
                    Place.withinReach(current.getCurrentPlace(), player.getCurrentPlace(), reach) >= 0) {
                List<Card> cards = player.getCards();
                if (!cards.isEmpty()) {
                    Card card = cards.get(ThreadLocalRandom.current().nextInt(cards.size()));
                    ci.removeCard(player, g, card);
                    ci.addCard(current, g, card);
                }
            }
            cb.run(_g, null);
        });
    }
}
