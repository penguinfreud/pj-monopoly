package monopoly.card;

import monopoly.*;
import monopoly.util.Consumer1;

public class GodOfLuckCard extends Card {
    private static final Card instance = new GodOfLuckCard();

    static {
        Game.putDefaultConfig("god-of-luck-card-price", 13);
        Game.putDefaultConfig("god-of-luck-card-duration", 8);
    }

    private GodOfLuckCard() {
        super("GodOfLuckCard");
    }

    public static void enable(Game g) {
        Cards.enableCard(g, instance);
        Properties.enable(g);
    }

    @Override
    public void use(Game g, Consumer1<Boolean> cb) {
        IPlayer player = g.getCurrentPlayer();
        Cards.get(player).addCard(Cards.getRandomCard(g, false));
        new RentFree(player, g.getConfig("god-of-luck-card-duration"));
        cb.accept(true);
    }
}
