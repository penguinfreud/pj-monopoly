package monopoly.card;

import monopoly.Card;
import monopoly.Game;
import monopoly.IPlayer;
import monopoly.extension.Lottery;
import monopoly.Cards;
import monopoly.util.Consumer1;

public class LotteryCard extends Card {
    static {
        registerCard(new LotteryCard());
        Game.putDefaultConfig("lottery-card-price", 9);
    }

    private LotteryCard() {
        super("LotteryCard");
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
