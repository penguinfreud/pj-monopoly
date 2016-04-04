package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.CardInterface;
import monopoly.Game;
import monopoly.Property;
import monopoly.util.Consumer0;

public class BuyLandCard extends Card {
    static {
        registerCard(new BuyLandCard());
        Game.putDefaultConfig("buy-land-card-price", 7);
    }

    private BuyLandCard() {
        super("BuyLandCard");
    }

    @Override
    public void use(Game g, CardInterface ci, Consumer0 cb) {
        AbstractPlayer player = g.getCurrentPlayer();
        Property prop = player.getCurrentPlace().asProperty();
        if (prop != null && prop.getOwner() != player) {
            ci.buyProperty(cb);
        }
        cb.run();
    }
}
