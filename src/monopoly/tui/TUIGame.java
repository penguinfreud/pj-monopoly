package monopoly.tui;

import monopoly.AbstractPlayer;
import monopoly.Game;

import java.util.Scanner;

public class TUIGame extends Game {
    private Scanner scanner = new Scanner(System.in);

    public TUIGame() {
        onO("gameOver", (o) -> {
            System.out.println(format("game_over", getCurrentPlayer().getName()));
        });
        onM("moneyChange", (e) -> {
            AbstractPlayer player = e.getPlayer();
            System.out.println(format(e.getMessage(),
                    player.getName(),
                    Math.abs(e.getAmount()),
                    player.getCurrentPlace().getName()));
        });
        onP("bankrupt", (p) -> {
            System.out.println(format("bankrupt", p.getName()));
        });
    }

    public Scanner getScanner() {
        return scanner;
    }
}
