package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Card;
import monopoly.Game;
import monopoly.util.Callback;

public class StayCard extends Card {
    static {
        registerCard(new StayCard());
        Game.putDefaultConfig("stay-card-price", 3);
    }

    private StayCard() {
        super("StayCard");
    }

    @Override
    public void use(Game g, AbstractPlayer.CardInterface ci, Callback<Object> cb) {
        ci.walk(g, 0);
    }
}
