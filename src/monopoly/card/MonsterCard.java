package monopoly.card;

import monopoly.Card;
import monopoly.Game;
import monopoly.Property;
import monopoly.place.Land;
import monopoly.place.Street;
import monopoly.util.Consumer1;

public class MonsterCard extends Card {
    static {
        registerCard(new MonsterCard());
        Game.putDefaultConfig("monster-card-price", 9);
    }

    private MonsterCard() {
        super("MonsterCard");
    }

    @Override
    public void use(Game g, Consumer1<Boolean> cb) {
        Property prop = g.getCurrentPlayer().getCurrentPlace().asProperty();
        if (prop != null && prop instanceof Land) {
            Street street = ((Land) prop).getStreet();
            street.getLands().stream().forEach(land -> land.resetLevel(g));
            cb.run(true);
        } else {
            g.triggerException("not_on_a_removable_land");
            cb.run(false);
        }
    }
}
