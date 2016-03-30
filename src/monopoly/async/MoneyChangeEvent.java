package monopoly.async;

import monopoly.AbstractPlayer;

public class MoneyChangeEvent {
    private AbstractPlayer player;
    private int amount;

    public MoneyChangeEvent(AbstractPlayer player, int amount) {
        this.player = player;
        this.amount = amount;
    }

    public AbstractPlayer getPlayer() {
        return player;
    }

    public int getAmount() {
        return amount;
    }
}