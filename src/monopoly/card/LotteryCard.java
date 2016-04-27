package monopoly.card;

import monopoly.*;
import monopoly.extension.Lottery;
import monopoly.util.Consumer1;

public class LotteryCard extends Card {
    private static final Card instance = new LotteryCard();

    static {
        Game.putDefaultConfig("lottery-card-price", 9);
    }

    private LotteryCard() {
        super("LotteryCard");
    }

    public static void enable(Game g) {
        Cards.enableCard(g, instance);
        Lottery.enable(g);
    }

    @Override
    protected void use(Game g, Consumer1<Boolean> cb) {
        IPlayer player = g.getCurrentPlayer();
        ((Cards.IPlayerWithCards) player).askForInt(getName(), number -> {
            int max = g.getConfig("lottery-number-max");
            if (number >= 0 && number <= max) {
                Lottery.cheat(g, number);
                cb.run(true);
            } else {
                cb.run(false);
            }
        });
    }
}
