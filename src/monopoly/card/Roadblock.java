package monopoly.card;

import monopoly.Card;
import monopoly.Cards;
import monopoly.Game;
import monopoly.IPlayer;
import monopoly.place.Place;
import monopoly.util.Consumer1;

public class Roadblock extends Card {
    private static final Card instance = new Roadblock();

    static {
        Game.putDefaultConfig("roadblock-price", 5);
        Game.putDefaultConfig("roadblock-reach", 8);
    }

    private Roadblock() {
        super("Roadblock");
    }

    public static void enable(Game g) {
        Cards.enableCard(g, instance);
    }

    @Override
    public void use(Game g, Consumer1<Boolean> cb) {
        IPlayer player = g.getCurrentPlayer();
        ((Cards.IPlayerWithCards) player).askForTargetPlace(getName(), place -> {
            synchronized (g.lock) {
                int reach = g.getConfig("roadblock-reach");
                if (place != null &&
                        Place.withinReach(player.getCurrentPlace(), place, reach) >= 0) {
                    place.setRoadblock(g);
                    cb.accept(true);
                } else {
                    cb.accept(false);
                }
            }
        });
    }
}
