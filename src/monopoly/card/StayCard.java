package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Card;
import monopoly.Game;
import monopoly.async.Callback;

class StayCard extends Card {
    static {
        Card.registerCard(new StayCard());
    }

    private StayCard() {
        super("StayCard");
    }

    @Override
    public void use(Game g, AbstractPlayer.CardInterface ci, Callback<Object> cb) {
        ci.stay(g);
    }
}
