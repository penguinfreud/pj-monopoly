package monopoly.card;

import monopoly.*;
import monopoly.util.Consumer0;
import monopoly.util.Consumer1;

public class Roadblock extends Card {
    static {
        registerCard(new Roadblock());
        Game.putDefaultConfig("roadblock-price", 5);
        Game.putDefaultConfig("roadblock-reach", 8);
    }

    private Roadblock() {
        super("Roadblock");
    }

    @Override
    public void use(Game g, Consumer1<Boolean> cb) {
        IPlayer player = g.getCurrentPlayer();
        ((Cards.IPlayerWithCards) player).askForTargetPlace(getName(), g.sync(place -> {
                int reach = g.getConfig("roadblock-reach");
                if (place != null &&
                        Place.withinReach(player.getCurrentPlace(), place, reach) >= 0) {
                    place.setRoadblock(g);
                    cb.run(true);
                } else {
                    cb.run(false);
                }
        }));
    }
}
