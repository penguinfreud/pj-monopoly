package monopoly.card;

import monopoly.Card;
import monopoly.Game;
import monopoly.place.Place;
import monopoly.IPlayer;
import monopoly.Property;
import monopoly.place.Land;
import monopoly.place.Street;
import monopoly.util.Consumer1;

public class TeardownCard extends Card {
    static {
        registerCard(new TeardownCard());
        Game.putDefaultConfig("teardown-card-price", 11);
    }

    private TeardownCard() {
        super("TeardownCard");
    }

    @Override
    public void use(Game g, Consumer1<Boolean> cb) {
        Place place = g.getCurrentPlayer().getCurrentPlace();
        Property prop = place.asProperty();
        if (prop != null && prop instanceof Land) {
            Street street = ((Land) prop).getStreet();
            for (Land land: street.getLands()) {
                IPlayer owner = land.getOwner();
                if (owner != null) {
                    int amount = land.getMortgagePrice() * 3/2;
                    String msg = g.format("teardown", owner.getName(), amount);
                    owner.changeCash(amount, msg);
                    land.resetOwner(g);
                }
                land.resetLevel(g);
            }
            cb.run(true);
        } else {
            g.triggerException("not_on_a_removable_land");
            cb.run(false);
        }
    }
}
