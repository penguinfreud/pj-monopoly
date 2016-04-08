package monopoly.card;

import monopoly.*;
import monopoly.util.Consumer0;
import monopoly.util.Consumer1;

public class BuyLandCard extends Card {
    static {
        registerCard(new BuyLandCard());
        Game.putDefaultConfig("buy-land-card-price", 7);
    }

    private BuyLandCard() {
        super("BuyLandCard");
    }

    @Override
    public void use(Game g, Consumer1<Boolean> cb) {
        IPlayer player = g.getCurrentPlayer();
        Property prop = player.getCurrentPlace().asProperty();
        if (prop != null && prop.getOwner() != player && player.getCash() >= prop.getPurchasePrice()) {
            Properties.get(player).buyProperty(prop, () -> cb.run(true), true);
        } else {
            cb.run(false);
        }
    }
}
