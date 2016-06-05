package monopoly.card;

import monopoly.Card;
import monopoly.Cards;
import monopoly.Game;
import monopoly.util.Consumer1;

public class StayCard extends Card {
    private static final Card instance = new StayCard();

    static {
        Game.putDefaultConfig("stay-card-price", 3);
    }

    private StayCard() {
        super("StayCard");
    }

    public static void enable(Game g) {
        Cards.enableCard(g, instance);
    }

    @Override
    public void use(Game g, Consumer1<Boolean> cb) {
        g.startWalking(0);
    }
}
