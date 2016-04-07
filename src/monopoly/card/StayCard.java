package monopoly.card;

import monopoly.Card;
import monopoly.Game;
import monopoly.util.Consumer0;

public class StayCard extends Card {
    static {
        registerCard(new StayCard());
        Game.putDefaultConfig("stay-card-price", 3);
    }

    private StayCard() {
        super("StayCard");
    }

    @Override
    public void use(Game g, Consumer0 cb) {
        g.startWalking(0);
    }
}
