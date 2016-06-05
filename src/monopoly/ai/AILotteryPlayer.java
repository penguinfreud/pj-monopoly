package monopoly.ai;

import monopoly.Game;
import monopoly.IPlayer;
import monopoly.extension.GameCalendar;
import monopoly.extension.Lottery;
import monopoly.util.Consumer0;

import java.util.Calendar;
import java.util.concurrent.ThreadLocalRandom;

public interface AILotteryPlayer extends IPlayer {
    default void buyLottery(int number) {
        Lottery.buyLottery(this, number);
    }

    @Override
    default void startTurn(Consumer0 cb) {
        Game g = getGame();
        if (GameCalendar.getField(g, Calendar.DATE) == 1) {
            int price = g.getConfig("lottery-price");
            if (getCash() > price) {
                int max = g.getConfig("lottery-number-max");
                buyLottery(ThreadLocalRandom.current().nextInt(max + 1));
            }
        }
        cb.accept();
    }
}
