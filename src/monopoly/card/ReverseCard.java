package monopoly.card;

import monopoly.*;
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
    public void use(Game g, Consumer0 cb) {
        IPlayer current = g.getCurrentPlayer();
        ((Cards.IPlayerWithCards) current).askForTargetPlayer(getName(), g.sync(player -> {
            int reach = g.getConfig("reverse-card-reach");
            if (player != null &&
                    Place.withinReach(current.getCurrentPlace(), player.getCurrentPlace(), reach) >= 0) {
                player.reverse();
            }
            cb.run();
        }));
    }
}
