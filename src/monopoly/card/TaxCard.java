package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Card;
import monopoly.Game;
import monopoly.Place;
import monopoly.util.Callback;

public class TaxCard extends Card {
    static {
        registerCard(new TaxCard());
        Game.putDefaultConfig("tax-card-price", 7);
        Game.putDefaultConfig("tax-card-reach", 5);
    }

    private TaxCard() {
        super("TaxCard");
    }

    @Override
    public void use(Game g, AbstractPlayer.CardInterface ci, Callback<Object> cb) {
        AbstractPlayer current = g.getCurrentPlayer();
        current.askForPlayer(g, getName(), (_g, player) -> {
            synchronized (ci.lock) {
                int reach = g.getConfig("tax-card-reach");
                if (player != null &&
                        Place.withinReach(current.getCurrentPlace(), player.getCurrentPlace(), reach) >= 0) {
                    ci.changeDeposit(player, g, player.getDeposit() * 3 / 10, "pay_tax");
                }
                cb.run(g, null);
            }
        });
    }
}
