package monopoly.extension;

import monopoly.Game;
import monopoly.IPlayer;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Lottery {
    static {
        Game.putDefaultConfig("lottery-number-max", 20);
        Game.putDefaultConfig("lottery-price", 200.0);
        Game.putDefaultConfig("lottery-pool", 2000.0);
    }

    private static final Map<Game, Lottery> parasites = new Hashtable<>();

    public static void enable(Game g) {
        if (parasites.get(g) == null) {
            GameCalendar.enable(g);
            parasites.put(g, new Lottery(g));
        }
    }

    public static boolean isEnabled(Game g) {
        return parasites.get(g) != null;
    }

    private final Game game;
    private double value;
    private int cheatNumber = -1;
    private final Map<Integer, List<IPlayer>> entries = new Hashtable<>();

    private Lottery(Game g) {
        game = g;
        value = g.getConfig("lottery-pool");
        GameCalendar.onMonth.get(g).addListener(this::selectLotteryWinner);
    }

    private void selectLotteryWinner() {
        int max = game.getConfig("lottery-number-max");
        int winningNumber = cheatNumber >= 0 ? cheatNumber : ThreadLocalRandom.current().nextInt(max);
        game.triggerException("lottery", winningNumber);
        List<IPlayer> winners = entries.get(winningNumber);
        if (winners != null) {
            double amount = value / winners.size();
            for (IPlayer player : winners) {
                String msg = game.format("win_lottery", player.getName(), amount);
                player.changeCash(amount, msg);
            }
        } else {
            game.triggerException("no_one_wins_lottery");
        }
        value = game.getConfig("lottery-pool");
        cheatNumber = -1;
        entries.clear();
    }

    public static void buyLottery(IPlayer player, int number) {
        Game g = player.getGame();
        double price = g.getConfig("lottery-price");
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

    public static void cheat(Game g, int number) {
        int max = g.getConfig("lottery-number-max");
        if (number >= 0 && number <= max) {
            parasites.get(g).cheatNumber = number;
        }
    }
}
