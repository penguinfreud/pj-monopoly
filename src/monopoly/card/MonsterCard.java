package monopoly.card;

import monopoly.*;
import monopoly.place.Land;
import monopoly.place.Street;
import monopoly.util.Consumer0;

public class MonsterCard extends Card {
    static {
        registerCard(new MonsterCard());
        Game.putDefaultConfig("monster-card-price", 9);
    }

    private MonsterCard() {
        super("MonsterCard");
    }

    @Override
    public void use(Game g, AbstractPlayer.CardInterface ci, Consumer0 cb) {
        Property prop = g.getCurrentPlayer().getCurrentPlace().asProperty();
        if (prop != null && prop instanceof Land) {
            Street street = ((Land) prop).getStreet();
            street.getLands().stream().forEach(ci::resetLevel);
        } else {
            g.triggerException("not_on_a_removable_land");
        }
        cb.run();
    }
}
