package monopoly.card;

import monopoly.*;
import monopoly.util.Consumer0;
import monopoly.util.Consumer1;

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
    public void use(Game g, Consumer1<Boolean> cb) {
        IPlayer player = g.getCurrentPlayer();
        Cards.get(player).addCard(Card.getRandomCard(g, false));
        new RentFree(player, g.getConfig("god-of-luck-card-duration"));
        cb.run(true);
    }
}
