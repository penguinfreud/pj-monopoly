package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.CardInterface;
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
    public void use(Game g, CardInterface ci, Consumer0 cb) {
        int sum = 0;
        List<AbstractPlayer> players = g.getPlayers();
        for (AbstractPlayer player: players) {
            sum += player.getCash();
        }
        sum /= players.size();
        for (AbstractPlayer player: players) {
            int amount = sum - player.getCash();
            String msg = g.format(amount >= 0? "equal_wealth_get": "equal_wealth_give", player.getName(), amount);
            ci.changeCash(player, amount, msg);
        }
        cb.run();
    }
}
