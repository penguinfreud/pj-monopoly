package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Card;
import monopoly.Game;
import monopoly.util.Consumer0;

public class GodOfFortuneCard extends Card {
    static {
        registerCard(new GodOfFortuneCard());
        Game.putDefaultConfig("god-of-fortune-card-price", 13);
        Game.putDefaultConfig("god-of-fortune-card-award", 10000);
        Game.putDefaultConfig("god-of-fortune-card-duration", 8);
    }

    private GodOfFortuneCard() {
        super("GodOfFortuneCard");
    }

    @Override
    public void use(Game g, Consumer0 cb) {
        AbstractPlayer player = g.getCurrentPlayer();
        int amount = g.getConfig("god-of-fortune-card-award");
        String msg = g.format("blessed_by_god_of_fortune", player.getName(), amount);
        player.changeCash(amount, msg);
        new RentFree(player, g.getConfig("god-of-fortune-card-duration"));
        cb.run();
    }
}
