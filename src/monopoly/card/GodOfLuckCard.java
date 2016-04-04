package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Card;
import monopoly.Game;
import monopoly.util.Callback;

public class GodOfLuckCard extends Card {
    static {
        registerCard(new GodOfLuckCard());
        Game.putDefaultConfig("godofluckcard-price", 13);
        Game.putDefaultConfig("god-of-luck-card-duration", 8);
    }

    private GodOfLuckCard() {
        super("GodOfLuckCard");
    }

    @Override
    public void use(Game g, AbstractPlayer.CardInterface ci, Callback<Object> cb) {
        AbstractPlayer player = g.getCurrentPlayer();
        ci.addCard(player, g, Card.getRandomCard(g, false));
        new RentFree(g, player, ci, g.getConfig("god-of-luck-card-duration"));
        cb.run(g, null);
    }
}
