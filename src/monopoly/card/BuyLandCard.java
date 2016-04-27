package monopoly.card;

import monopoly.*;
import monopoly.util.Consumer1;

public class BuyLandCard extends Card {
    private static final Card instance = new BuyLandCard();
    static {
        Game.putDefaultConfig("buy-land-card-price", 7);
    }

    private BuyLandCard() {
        super("BuyLandCard");
    }

    public static void enable(Game g) {
        Cards.enableCard(g, instance);
        Properties.enable(g);
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
