package monopoly.tui;

import monopoly.*;
import monopoly.extension.BankSystem;
import monopoly.extension.Lottery;
import monopoly.stock.StockMarket;

import java.util.Scanner;

public class TUIGame extends Game {
    private final Scanner scanner = new Scanner(System.in);

    public TUIGame() {
        super();

        Properties.init(this);
        Cards.init(this);
        BankSystem.init(this);
        Lottery.init(this);
        StockMarket.init(this);
        Shareholding.init(this);
        Card.enableAll(this);

        onGameOver.addListener(() ->
            System.out.println(format("game_over", getCurrentPlayer().getName())));
        onLanded.addListener (() ->
            System.out.println(format("you_have_arrived",
                    getCurrentPlayer().getCurrentPlace().toString(this))));
        onBankrupt.addListener((p) ->
            System.out.println(format("bankrupt", p.getName())));
        onException.addListener(System.out::println);
        BasePlayer.onMoneyChange.get(this).addListener((player, amount, msg) -> {
            if (!msg.isEmpty()) {
                System.out.println(msg);
            }
        });
        Cards.onCouponChange.get(this).addListener((player, amount) -> {
            if (amount > 0) {
                System.out.println(format("get_coupons", player.getName(), amount));
            }
        });
        Cards.onCardChange.get(this).addListener((player, isGet, card) -> {
            if (isGet) {
                System.out.println(format("get_card", player.getName(), card.toString(this)));
            }
        });
    }

    public Scanner getScanner() {
        return scanner;
    }
}
