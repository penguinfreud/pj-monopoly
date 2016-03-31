package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Card;
import monopoly.Game;
import monopoly.async.Callback;

public class ReverseCard extends Card {
    @Override
    public void use(Game g, AbstractPlayer.CardInterface ci, Callback<Object> cb) {
        ci.reverse(g.getCurrentPlayer());
        cb.run(null);
    }
}
