package monopoly.card;

import monopoly.*;
import monopoly.place.Land;
import monopoly.place.Place;
import monopoly.place.Street;
import monopoly.util.Consumer1;

public class TeardownCard extends Card {
    private static final Card instance = new TeardownCard();

    static {
        Game.putDefaultConfig("teardown-card-price", 11);
    }

    private TeardownCard() {
        super("TeardownCard");
    }

    public static void enable(Game g) {
        Cards.enableCard(g, instance);
        Properties.enable(g);
    }

    @Override
    public void use(Game g, Consumer1<Boolean> cb) {
        Place place = g.getCurrentPlayer().getCurrentPlace();
        Property prop = place.asProperty();
        if (prop != null && prop instanceof Land) {
            Street street = ((Land) prop).getStreet();
            for (Land land : street.getLands()) {
                IPlayer owner = land.getOwner();
                if (owner != null) {
                    double amount = land.getMortgagePrice() * 3 / 2;
                    String msg = g.format("teardown", owner.getName(), amount);
                    owner.changeCash(amount, msg);
                    land.resetOwner(g);
                    Properties.get(owner).removeLand(prop);
                }
                land.resetLevel(g);
            }
            cb.accept(true);
        } else {
            g.triggerException("not_on_a_removable_land");
            cb.accept(false);
        }
    }
}
