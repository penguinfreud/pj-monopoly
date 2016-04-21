package monopoly.card;

import monopoly.Card;
import monopoly.Game;
import monopoly.Cards;
import monopoly.IPlayer;
import monopoly.place.Place;
import monopoly.util.Consumer1;

public class TaxCard extends Card {
    private static final Card instance = new TaxCard();
    static {
        Game.putDefaultConfig("tax-card-price", 7);
        Game.putDefaultConfig("tax-card-reach", 5);
    }

    private TaxCard() {
        super("TaxCard");
    }

    public static void enable(Game g) {
        Cards.enableCard(g, instance);
    }

    @Override
    public void use(Game g, Consumer1<Boolean> cb) {
        IPlayer current = g.getCurrentPlayer();
        ((Cards.IPlayerWithCards) current).askForTargetPlayer(getName(), player -> {
            synchronized (g.lock) {
                int reach = g.getConfig("tax-card-reach");
                if (player != null &&
                        Place.withinReach(current.getCurrentPlace(), player.getCurrentPlace(), reach) >= 0) {
                    int amount = player.getDeposit() * 3 / 10;
                    String msg = g.format("pay_tax", player.getName(), amount);
                    player.changeDeposit(-amount, msg);
                    cb.run(true);
                } else {
                    cb.run(false);
                }
            }
        });
    }
}
