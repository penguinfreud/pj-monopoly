package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.CardInterface;
import monopoly.Game;
import monopoly.Place;
import monopoly.util.Consumer0;

public class ReverseCard extends Card {
    static {
        registerCard(new ReverseCard());
        Game.putDefaultConfig("reverse-card-price", 3);
        Game.putDefaultConfig("reverse-card-reach", 5);
    }

    private ReverseCard() {
        super("ReverseCard");
    }

    @Override
    public void use(Game g, CardInterface ci, Consumer0 cb) {
        AbstractPlayer current = g.getCurrentPlayer();
        current.askForPlayer(getName(), (player) -> {
            synchronized (ci.lock) {
                int reach = g.getConfig("reverse-card-reach");
                if (player != null &&
                        Place.withinReach(current.getCurrentPlace(), player.getCurrentPlace(), reach) >= 0) {
                    ci.reverse(player);
                }
                cb.run();
            }
        });
    }
}
