package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Game;
import monopoly.util.Consumer0;

public class GodOfLuckCard extends Card {
    static {
        registerCard(new GodOfLuckCard());
        Game.putDefaultConfig("god-of-luck-card-price", 13);
        Game.putDefaultConfig("god-of-luck-card-duration", 8);
    }

    private GodOfLuckCard() {
        super("GodOfLuckCard");
    }

    @Override
    public void use(Game g, AbstractPlayer.CardInterface ci, Consumer0 cb) {
        AbstractPlayer player = g.getCurrentPlayer();
        ci.addCard(player, Card.getRandomCard(g, false));
        new RentFree(g, player, ci, g.getConfig("god-of-luck-card-duration"));
        cb.run();
    }
}
