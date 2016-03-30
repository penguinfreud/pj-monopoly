package monopoly.async;

import monopoly.AbstractPlayer;

public class MoneyChangeEvent {
    private AbstractPlayer player;
    private int amount;
    private String msg;

    public MoneyChangeEvent(AbstractPlayer player, int amount, String msg) {
        this.player = player;
        this.amount = amount;
        this.msg = msg;
    }

    public AbstractPlayer getPlayer() {
        return player;
    }

    public int getAmount() {
        return amount;
    }

    public String getMessage() {
        return msg;
    }
}