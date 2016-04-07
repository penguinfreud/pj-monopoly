package monopoly.tui;

import monopoly.AbstractPlayer;
import monopoly.Cards;
import monopoly.Game;

import java.util.Scanner;

public class TUIGame extends Game {
    private final Scanner scanner = new Scanner(System.in);

    public TUIGame() {
        super();
        onGameOver.addListener(this, () ->
            System.out.println(format("game_over", getCurrentPlayer().getName())));
        onLanded.addListener (this, () ->
            System.out.println(format("you_have_arrived",
                    getCurrentPlayer().getCurrentPlace().toString(this))));
        onBankrupt.addListener(this, (p) ->
            System.out.println(format("bankrupt", p.getName())));
        onException.addListener(this, System.out::println);
        AbstractPlayer.onMoneyChange.addListener(this, (player, amount, msg) -> {
            if (!msg.isEmpty()) {
                System.out.println(msg);
            }
        });
        Cards.onCouponChange.addListener(this, (player, amount) -> {
            if (amount > 0) {
                System.out.println(format("get_coupons", player.getName(), amount));
            }
        });
        Cards.onCardChange.addListener(this, (player, isGet, card) -> {
            if (isGet) {
                System.out.println(format("get_card", player.getName(), card.toString(this)));
            }
        });
    }

    public Scanner getScanner() {
        return scanner;
    }
}
