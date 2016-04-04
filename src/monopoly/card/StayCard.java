package monopoly.card;

import monopoly.AbstractPlayer;
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
    public void use(Game g, AbstractPlayer.CardInterface ci, Consumer0 cb) {
        ci.walk(0);
    }
}
