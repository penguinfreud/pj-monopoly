package monopoly.card;

import monopoly.*;
import monopoly.util.Consumer0;

public class LotteryCard extends Card {
    static {
        registerCard(new LotteryCard());
        Game.putDefaultConfig("lottery-card-price", 9);
    }

    private LotteryCard() {
        super("LotteryCard");
    }

    @Override
    protected void use(Game g, Consumer0 cb) {
        IPlayer player = g.getCurrentPlayer();
        int price = g.getConfig("lottery-price");
        if (player.getCash() > price) {
            ((Cards.IPlayerWithCards) player).askForInt(getName(), number -> {
                Lottery.buyLottery(player, number);
                cb.run();
            });
        } else {
            cb.run();
        }
    }
}
