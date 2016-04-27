package monopoly.tui;

import monopoly.*;
import monopoly.extension.BankSystem;
import monopoly.extension.Lottery;
import monopoly.stock.StockMarket;
import monopoly.util.Parasite;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class TUI {
    private TUI() {}

    private static final Object scannerLock = new Object(),
                                outputLock = new Object();
    private static final Map<InputStream, Scanner> scanners = new Hashtable<>();
    private static final Map<PrintStream, List<Game>> outputAssociations = new Hashtable<>();

    static Scanner getScanner(InputStream in) {
        synchronized (scannerLock) {
            Scanner scanner = scanners.get(in);
            if (scanner == null) {
                scanner = new Scanner(in);
                scanners.put(in, scanner);
            }
            return scanner;
        }
    }

    static void addOutput(Game g, PrintStream out) {
        synchronized (outputLock) {
            List<Game> games = outputAssociations.get(out);
            if (games == null) {
                games = new CopyOnWriteArrayList<>();
                outputAssociations.put(out, games);
            }

            if (!games.contains(g)) {
                games.add(g);
                g.onGameOver.addListener(() ->
                        out.println(g.format("game_over", g.getCurrentPlayer().getName())));
                g.onLanded.addListener(() ->
                        out.println(g.format("you_have_arrived",
                                g.getCurrentPlayer().getCurrentPlace().toString(g))));
                g.onBankrupt.addListener((p) ->
                        out.println(g.format("bankrupt", p.getName())));
                g.onException.addListener(System.out::println);

                BasePlayer.onMoneyChange.get(g).addListener((player, amount, msg) -> {
                    if (!msg.isEmpty()) {
                        out.println(msg);
                    }
                });
                g.onGameStart.addListener(() -> {
                    if (Cards.isEnabled(g)) {
                        Cards.onCouponChange.get(g).addListener((player, amount) -> {
                            if (amount > 0) {
                                out.println(g.format("get_coupons", player.getName(), amount));
                            }
                        });
                        Cards.onCardChange.get(g).addListener((player, isGet, card) -> {
                            if (isGet) {
                                out.println(g.format("get_card", player.getName(), card.toString(g)));
                            }
                        });
                    }
                });
            }
        }
    }
}
