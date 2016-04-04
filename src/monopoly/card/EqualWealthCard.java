package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Card;
import monopoly.Game;
import monopoly.util.Callback;

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
    public void use(Game g, AbstractPlayer.CardInterface ci, Callback<Object> cb) {
        int sum = 0;
        List<AbstractPlayer> players = g.getPlayers();
        for (AbstractPlayer player: players) {
            sum += player.getCash();
        }
        sum /= players.size();
        for (AbstractPlayer player: players) {
            int amount = sum - player.getCash();
            ci.changeCash(player, g, amount, amount >= 0? "equal_wealth_get": "equal_wealth_give");
        }
        cb.run(g, null);
    }
}
