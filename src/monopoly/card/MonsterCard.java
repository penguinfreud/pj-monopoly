package monopoly.card;

import monopoly.*;
import monopoly.place.Land;
import monopoly.place.Street;
import monopoly.util.Callback;

public class MonsterCard extends Card {
    static {
        registerCard(new MonsterCard());
        Game.putDefaultConfig("monstercard-price", 9);
    }

    private MonsterCard() {
        super("MonsterCard");
    }

    @Override
    public void use(Game g, AbstractPlayer.CardInterface ci, Callback<Object> cb) {
        Property prop = g.getCurrentPlayer().getCurrentPlace().asProperty();
        if (prop != null && prop instanceof Land) {
            Street street = ((Land) prop).getStreet();
            for (Land land: street.getLands()) {
                ci.resetLevel(g, land);
            }
        } else {
            g.triggerException("not_on_a_removable_land");
        }
        cb.run(g, null);
    }
}
