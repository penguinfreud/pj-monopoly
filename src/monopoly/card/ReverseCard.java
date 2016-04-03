package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Card;
import monopoly.Game;
import monopoly.Place;
import monopoly.util.Callback;

class ReverseCard extends Card {
    static {
        registerCard(new ReverseCard());
        Game.putDefaultConfig("reversecard-price", 3);
    }

    private ReverseCard() {
        super("ReverseCard");
    }

    @Override
    public void use(Game g, AbstractPlayer.CardInterface ci, Callback<Object> cb) {
        AbstractPlayer current = g.getCurrentPlayer();
        current.askForPlayer(g, getName(), (_g, player) -> {
            synchronized (ci.lock) {
                int reach = _g.getConfig("reversecard-reach");
                if (player != null &&
                        Place.withinReach(current.getCurrentPlace(), player.getCurrentPlace(), reach) >= 0) {
                    ci.reverse(player);
                }
                cb.run(_g, null);
            }
        });
    }
}
