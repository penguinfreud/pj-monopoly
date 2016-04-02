package monopoly.tui;

import monopoly.AbstractPlayer;
import monopoly.Game;

import java.util.Scanner;

public class TUIGame extends Game {
    private Scanner scanner = new Scanner(System.in);

    public TUIGame() {
        onO("gameOver", (g, o) ->
            System.out.println(g.format("game_over", g.getCurrentPlayer().getName())));
        onO("landed", (g, o) ->
            System.out.println(g.format("you_have_arrived",
                    g.getCurrentPlayer().getCurrentPlace().toString(this))));
        onM("moneyChange", (g, e) -> {
            AbstractPlayer player = e.getPlayer();
            System.out.println(g.format(e.getMessage(),
                    player.getName(),
                    Math.abs(e.getAmount()),
                    player.getCurrentPlace().getName()));
        });
        onP("bankrupt", (g, p) ->
            System.out.println(g.format("bankrupt", p.getName())));
    }

    public Scanner getScanner() {
        return scanner;
    }
}
