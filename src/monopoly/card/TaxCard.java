package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.CardInterface;
import monopoly.Game;
import monopoly.Place;
import monopoly.util.Consumer0;

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
    public void use(Game g, CardInterface ci, Consumer0 cb) {
        AbstractPlayer current = g.getCurrentPlayer();
        current.askForPlayer(getName(), (player) -> {
            synchronized (ci.lock) {
                int reach = g.getConfig("tax-card-reach");
                if (player != null &&
                        Place.withinReach(current.getCurrentPlace(), player.getCurrentPlace(), reach) >= 0) {
                    int amount = player.getDeposit() * 3 / 10;
                    String msg = g.format("pay_tax", player.getName(), amount);
                    ci.changeDeposit(player, -amount, msg);
                }
                cb.run();
            }
        });
    }
}
