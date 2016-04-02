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
        AbstractPlayer.onMoneyChange.addListener((g, e) -> {
            AbstractPlayer player = e.getFirst();
            System.out.println(g.format(e.getThird(),
                    player.getName(),
                    Math.abs(e.getSecond()),
                    player.getCurrentPlace().getName()));
        });
        onBankrupt.addListener((g, p) ->
            System.out.println(g.format("bankrupt", p.getName())));
    }

    public Scanner getScanner() {
        return scanner;
    }
}
