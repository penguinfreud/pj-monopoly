package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Card;
import monopoly.Game;
import monopoly.util.Callback;

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
    public void use(Game g, AbstractPlayer.CardInterface ci, Callback<Object> cb) {
        AbstractPlayer player = g.getCurrentPlayer();
        ci.changeCash(player, g, g.getConfig("god-of-fortune-card-award"), "blessed_by_god_of_fortune");
        new RentFree(g, player, ci, g.getConfig("god-of-fortune-card-duration"));
        cb.run(g, null);
    }
}
