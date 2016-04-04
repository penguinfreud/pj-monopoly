package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.CardInterface;
import monopoly.Game;
import monopoly.util.Consumer0;

public class GodOfFortuneCard extends Card {
    static {
        registerCard(new GodOfFortuneCard());
        Game.putDefaultConfig("god-of-fortune-card-price", 13);
        Game.putDefaultConfig("god-of-fortune-card-award", 10000);
        Game.putDefaultConfig("god-of-fortune-card-duration", 8);
    }

    private GodOfFortuneCard() {
        super("GodOfFortuneCard");
    }

    @Override
    public void use(Game g, CardInterface ci, Consumer0 cb) {
        AbstractPlayer player = g.getCurrentPlayer();
        ci.changeCash(player, g.getConfig("god-of-fortune-card-award"), "blessed_by_god_of_fortune");
        new RentFree(g, player, ci, g.getConfig("god-of-fortune-card-duration"));
        cb.run();
    }
}
