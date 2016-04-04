package monopoly.tui;

import monopoly.AbstractPlayer;
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
        AbstractPlayer.onMoneyChange.addListener(this, (e) -> {
            AbstractPlayer player = e.getFirst();
            System.out.println(format(e.getThird(),
                    player.getName(),
                    Math.abs(e.getSecond()),
                    player.getCurrentPlace().getName()));
        });
        AbstractPlayer.onGetCoupons.addListener(this, (player, amount) ->
            System.out.println(format("get_coupons", amount)));
        AbstractPlayer.onGetCard.addListener(this, (player, card) ->
            System.out.println(format("get_card", card.toString(this))));
    }

    public Scanner getScanner() {
        return scanner;
    }
}
