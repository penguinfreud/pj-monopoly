package monopoly.tui;

import monopoly.*;
import monopoly.extension.BankSystem;
import monopoly.extension.Lottery;
import monopoly.stock.StockMarket;
import monopoly.util.Parasite;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class TUI {
    private static final Parasite<Game, TUI> parasites = new Parasite<>("TUI");

    public static TUI get(Game g) {
        return parasites.get(g);
    }

    public static void enable(Game g, InputStream in, PrintStream out) {
        if (parasites.get(g) == null) {
            parasites.set(g, new TUI(g, in, out));
        }
    }

    public static boolean isEanbled(Game g) {
        return parasites.get(g) != null;
    }

    private final Game game;
    private final PrintStream out;
    private final Scanner scanner;

    private TUI(Game g, InputStream in, PrintStream out) {
        super();

        game = g;
        scanner = new Scanner(in);
        this.out = out;

        g.onGameOver.addListener(() ->
            out.println(g.format("game_over", g.getCurrentPlayer().getName())));
        g.onLanded.addListener (() ->
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
    }

    Scanner getScanner() {
        return scanner;
    }
    PrintStream getOut() {
        return out;
    }
}
