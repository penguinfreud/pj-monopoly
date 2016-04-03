package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Card;
import monopoly.Game;
import monopoly.Place;
import monopoly.util.Callback;

class Roadblock extends Card {
    static {
        registerCard(new Roadblock());
        Game.putDefaultConfig("roadblock-price", 5);
        Game.putDefaultConfig("roadblock-reach", 8);
    }

    private Roadblock() {
        super("Roadblock");
    }

    public void use(Game g, AbstractPlayer.CardInterface ci, Callback<Object> cb) {
        g.getCurrentPlayer().askForPlace(g, getName(), (_g, place) -> {
            synchronized (ci.lock) {
                int reach = _g.getConfig("roadblock-reach");
                if (place != null &&
                        Place.withinReach(_g.getCurrentPlayer().getCurrentPlace(), place, reach) >= 0) {
                    ci.setRoadblock(_g, place);
                }
                cb.run(_g, null);
            }
        });
    }
}
