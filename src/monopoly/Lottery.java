package monopoly;

import monopoly.util.Parasite;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Lottery {
    static {
        Game.putDefaultConfig("lottery-number-max", 20);
        Game.putDefaultConfig("lottery-price", 200);
    }

    private static final Parasite<Game, Lottery> parasites = new Parasite<>("Lottery", Game::onInit, Lottery::new);

    private final Game game;
    private int value = 0;
    private final Map<Integer, List<IPlayer>> entries = new Hashtable<>();

    private Lottery(Game g) {
        game = g;
        GameCalendar.onMonth.addListener(g, this::selectLotteryWinner);
    }

    private void selectLotteryWinner() {
        int max = game.getConfig("lottery-number-max");
        int winningNumber = ThreadLocalRandom.current().nextInt(max);
        game.triggerException("lottery", winningNumber);
        List<IPlayer> winners = entries.get(winningNumber);
        if (winners != null) {
            int amount = value / winners.size();
            for (IPlayer player: winners) {
                String msg = game.format("win_lottery", player.getName(), amount);
                player.changeCash(amount, msg);
            }
        } else {
            game.triggerException("no_one_wins_lottery");
        }
        value = 0;
        entries.clear();
    }

    public static void buyLottery(IPlayer player, int number) {
        Game g = player.getGame();
        int price = g.getConfig("lottery-price");
        if (player.getCash() >= price) {
            Lottery lottery = parasites.get(g);
            Map<Integer, List<IPlayer>> entries = lottery.entries;

            if (number < 0 || number > (Integer) g.getConfig("lottery-number-max")) {
                g.triggerException("invalid_lottery_number");
            } else {
                List<IPlayer> betters = entries.get(number);
                if (betters == null) {
                    betters = new CopyOnWriteArrayList<>();
                    entries.put(number, betters);
                }

                betters.add(player);
                player.changeCash(-price, g.format("buy_lottery", player.getName(), price));
                lottery.value += price;
            }
        } else {
            g.triggerException("short_of_cash");
        }
    }
}