package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Card;
import monopoly.Game;
import monopoly.util.Callback;

class StayCard extends Card {
    static {
        registerCard(new StayCard());
        Game.putDefaultConfig("staycard-price", 3);
    }

    private StayCard() {
        super("StayCard");
    }

    @Override
    public void use(Game g, AbstractPlayer.CardInterface ci, Callback<Object> cb) {
        ci.stay(g);
    }
}
