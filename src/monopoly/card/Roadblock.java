package monopoly.card;

import monopoly.CardInterface;
import monopoly.Game;
import monopoly.Place;
import monopoly.util.Consumer0;

public class Roadblock extends Card {
    static {
        registerCard(new Roadblock());
        Game.putDefaultConfig("roadblock-price", 5);
        Game.putDefaultConfig("roadblock-reach", 8);
    }

    private Roadblock() {
        super("Roadblock");
    }

    public void use(Game g, CardInterface ci, Consumer0 cb) {
        g.getCurrentPlayer().askForPlace(getName(), (place) -> {
            synchronized (ci.lock) {
                int reach = g.getConfig("roadblock-reach");
                if (place != null &&
                        Place.withinReach(g.getCurrentPlayer().getCurrentPlace(), place, reach) >= 0) {
                    ci.setRoadblock(place);
                }
                cb.run();
            }
        });
    }
}
