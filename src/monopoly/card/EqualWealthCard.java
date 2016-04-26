package monopoly.card;

import monopoly.Card;
import monopoly.Cards;
import monopoly.Game;
import monopoly.IPlayer;
import monopoly.util.Consumer0;
import monopoly.util.Consumer1;

import java.util.List;

public class EqualWealthCard extends Card {
    private static final Card instance = new EqualWealthCard();
    static {
        Game.putDefaultConfig("equal-wealth-card-price", 11);
    }

    private EqualWealthCard() {
        super("EqualWealthCard");
    }

    public static void enable(Game g) {
        Cards.enableCard(g, instance);
    }

    @Override
    public void use(Game g, Consumer1<Boolean> cb) {
        List<IPlayer> players = g.getPlayers();
        double average = players.stream().map(IPlayer::getCash).reduce(0.0, (a, b) -> a + b) / players.size();
        for (IPlayer player: players) {
            double amount = average - player.getCash();
            String msg;
            if (amount >= 0) {
                msg = g.format("equal_wealth_get", player.getName(), amount);
            } else {
                msg = g.format("equal_wealth_give", player.getName(), -amount);
            }
            player.changeCash(amount, msg);
        }
        cb.run(true);
    }
}
