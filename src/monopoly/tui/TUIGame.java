package monopoly.tui;

import monopoly.AbstractPlayer;
import monopoly.Game;

import java.text.MessageFormat;
import java.util.Scanner;

public class TUIGame extends Game {
    private Scanner scanner = new Scanner(System.in);

    private void print(String key, Object ... args) {
        System.out.println(MessageFormat.format(getText(key), args));
    }

    private String format(String pattern, Object ... args) {
        return MessageFormat.format(pattern, args);
    }

    public TUIGame() {
        onO("gameOver", (o) -> {
            print("game_over", getCurrentPlayer().getName());
        });
        onM("moneyChange", (e) -> {
            AbstractPlayer player = e.getPlayer();
            System.out.println(format(e.getMessage(),
                    player.getName(),
                    Math.abs(e.getAmount()),
                    player.getCurrentPlace().getName()));
        });
        onP("bankrupt", (p) -> {
            print("bankrupt", p.getName());
        });
    }

    public Scanner getScanner() {
        return scanner;
    }
}
