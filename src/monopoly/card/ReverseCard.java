package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Card;
import monopoly.Game;
import monopoly.async.Callback;

class ReverseCard extends Card {
    static {
        Card.registerCard(new ReverseCard());
    }

    private ReverseCard() {
        super("ReverseCard");
    }

    @Override
    public void use(Game g, AbstractPlayer.CardInterface ci, Callback<Object> cb) {
        g.getCurrentPlayer().askWhomToReverse(g, (player) -> {
            if (player != null) {
                ci.reverse(player);
            }
            cb.run(null);
        });
    }
}
