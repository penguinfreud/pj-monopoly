package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Card;
import monopoly.Game;
import monopoly.util.Consumer0;

import java.util.List;

public class EqualWealthCard extends Card {
    static {
        registerCard(new EqualWealthCard());
        Game.putDefaultConfig("equal-wealth-card-price", 11);
    }

    private EqualWealthCard() {
        super("EqualWealthCard");
    }

    @Override
    public void use(Game g, Consumer0 cb) {
        List<AbstractPlayer> players = g.getPlayers();
        int average = players.stream().map(AbstractPlayer::getCash).reduce(0, (a, b) -> a + b) / players.size();
        for (AbstractPlayer player: players) {
            int amount = average - player.getCash();
            String msg = g.format(amount >= 0? "equal_wealth_get": "equal_wealth_give", player.getName(), amount);
            player.changeCash(amount, msg);
        }
        cb.run();
    }
}
