package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Card;
import monopoly.Game;
import monopoly.Place;
import monopoly.async.Callback;

class Roadblock extends Card {
    static {
        Card.registerCard(new Roadblock());
    }

    private Roadblock() {
        super("Roadblock");
    }

    public void use(Game g, AbstractPlayer.CardInterface ci, Callback<Object> cb) {
        g.getCurrentPlayer().askWhereToSetRoadblock(g, (_g, place) -> {
            int reach = (Integer) _g.getConfig("roadblock-reach");
            if (place != null &&
                    Place.withinReach(_g.getCurrentPlayer().getCurrentPlace(), place, reach) >= 0) {
                ci.setRoadblock(_g, place);
            }
            cb.run(_g, null);
        });
    }
}
