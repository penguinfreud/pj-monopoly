package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Card;
import monopoly.Game;
import monopoly.Property;
import monopoly.async.Callback;

public class BuyLandCard extends Card {
    static {
        Card.registerCard(new BuyLandCard());
        Game.putDefaultConfig("card-buylandcard-price", 7);
    }

    private BuyLandCard() {
        super("BuyLandCard");
    }

    @Override
    public void use(Game g, AbstractPlayer.CardInterface ci, Callback<Object> cb) {
        AbstractPlayer player = g.getCurrentPlayer();
        Property prop = player.getCurrentPlace().asProperty();
        if (prop != null && prop.getOwner() != player) {
            ci.buyProperty(g, cb);
        }
        cb.run(g, null);
    }
}
