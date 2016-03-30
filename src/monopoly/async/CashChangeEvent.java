package monopoly.async;

import monopoly.AbstractPlayer;

public class CashChangeEvent {
    private AbstractPlayer player;
    private int amount;

    public CashChangeEvent(AbstractPlayer player, int amount) {
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