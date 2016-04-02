package monopoly.tui;

import monopoly.AbstractPlayer;
import monopoly.Game;

import java.util.Scanner;

public class TUIGame extends Game {
    private final Scanner scanner = new Scanner(System.in);

    static {
        onGameOver.addListener((g, o) ->
            System.out.println(g.format("game_over", g.getCurrentPlayer().getName())));
        onLanded.addListener ((g, o) ->
            System.out.println(g.format("you_have_arrived",
                    g.getCurrentPlayer().getCurrentPlace().toString(g))));
        onBankrupt.addListener((g, p) ->
            System.out.println(g.format("bankrupt", p.getName())));
        AbstractPlayer.onMoneyChange.addListener((g, e) -> {
            AbstractPlayer player = e.getFirst();
            System.out.println(g.format(e.getThird(),
                    player.getName(),
                    Math.abs(e.getSecond()),
                    player.getCurrentPlace().getName()));
        });
        AbstractPlayer.onGetCoupons.addListener((g, e) -> {
            System.out.println(g.format("get_coupons", e.getSecond()));
        });
        AbstractPlayer.onGetCard.addListener((g, e) -> {
            System.out.println(g.format("get_card", e.getSecond().toString(g)));
        });
    }

    public Scanner getScanner() {
        return scanner;
    }
}
