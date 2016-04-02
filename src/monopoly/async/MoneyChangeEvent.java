package monopoly.async;

import monopoly.AbstractPlayer;

public class MoneyChangeEvent {
    private final AbstractPlayer player;
    private final int amount;
    private final String msg;

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