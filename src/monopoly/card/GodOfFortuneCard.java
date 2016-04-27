package monopoly.card;

import monopoly.Game;
import monopoly.Card;
import monopoly.Cards;
import monopoly.Properties;
import monopoly.IPlayer;
import monopoly.util.Consumer1;

public class GodOfFortuneCard extends Card {
    private static final Card instance = new GodOfFortuneCard();
    static {
        Game.putDefaultConfig("god-of-fortune-card-price", 13);
        Game.putDefaultConfig("god-of-fortune-card-award", 10000.0);
        Game.putDefaultConfig("god-of-fortune-card-duration", 8);
    }

    private GodOfFortuneCard() {
        super("GodOfFortuneCard");
    }

    public static void enable(Game g) {
        Cards.enableCard(g, instance);
        Properties.enable(g);
    }

    @Override
    public void use(Game g, Consumer1<Boolean> cb) {
        IPlayer player = g.getCurrentPlayer();
        double amount = g.getConfig("god-of-fortune-card-award");
        String msg = g.format("blessed_by_god_of_fortune", player.getName(), amount);
        player.changeCash(amount, msg);
        new RentFree(player, g.getConfig("god-of-fortune-card-duration"));
        cb.run(true);
    }
}
