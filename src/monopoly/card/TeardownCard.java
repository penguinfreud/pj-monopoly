package monopoly.card;

import monopoly.*;
import monopoly.place.Land;
import monopoly.place.Street;
import monopoly.util.Callback;

public class TeardownCard extends Card {
    static {
        registerCard(new TeardownCard());
        Game.putDefaultConfig("teardown-card-price", 11);
    }

    private TeardownCard() {
        super("TeardownCard");
    }

    @Override
    public void use(Game g, AbstractPlayer.CardInterface ci, Callback<Object> cb) {
        Place place = g.getCurrentPlayer().getCurrentPlace();
        Property prop = place.asProperty();
        if (prop != null && prop instanceof Land) {
            Street street = ((Land) prop).getStreet();
            for (Land land: street.getLands()) {
                AbstractPlayer owner = land.getOwner();
                if (owner != null) {
                    ci.changeCash(owner, g, land.getMortgagePrice() * 3/2, "teardown");
                    ci.resetOwner(g, land);
                }
                ci.resetLevel(g, land);
            }
        } else {
            g.triggerException("not_on_a_removable_land");
        }
        cb.run(g, null);
    }
}
