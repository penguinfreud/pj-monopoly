package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Card;
import monopoly.Game;
import monopoly.async.Callback;

public class StayCard extends Card {
    @Override
    public void use(Game g, AbstractPlayer.CardInterface ci, Callback<Object> cb) {
        ci.stay(g);
    }
}
