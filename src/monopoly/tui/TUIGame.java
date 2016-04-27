package monopoly.tui;

import monopoly.*;
import monopoly.extension.BankSystem;
import monopoly.extension.Lottery;
import monopoly.stock.StockMarket;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class TUIGame extends Game {
    private final PrintStream out;
    private final Scanner scanner;

    public TUIGame(InputStream in, PrintStream out) {
        super();

        this.out = out;
        scanner = new Scanner(in);

        Properties.init(this);
        Cards.init(this);
        BankSystem.init(this);
        Lottery.init(this);
        StockMarket.init(this);
        Shareholding.init(this);
        Card.enableAll(this);

        onGameOver.addListener(() ->
            out.println(format("game_over", getCurrentPlayer().getName())));
        onLanded.addListener (() ->
            out.println(format("you_have_arrived",
                    getCurrentPlayer().getCurrentPlace().toString(this))));
        onBankrupt.addListener((p) ->
            out.println(format("bankrupt", p.getName())));
        onException.addListener(System.out::println);
        BasePlayer.onMoneyChange.get(this).addListener((player, amount, msg) -> {
            if (!msg.isEmpty()) {
                out.println(msg);
            }
        });
        Cards.onCouponChange.get(this).addListener((player, amount) -> {
            if (amount > 0) {
                out.println(format("get_coupons", player.getName(), amount));
            }
        });
        Cards.onCardChange.get(this).addListener((player, isGet, card) -> {
            if (isGet) {
                out.println(format("get_card", player.getName(), card.toString(this)));
            }
        });
    }

    Scanner getScanner() {
        return scanner;
    }
    PrintStream getOut() {
        return out;
    }
}
