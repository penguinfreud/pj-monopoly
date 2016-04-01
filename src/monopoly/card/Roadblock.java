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
        g.getCurrentPlayer().askWhereToSetRoadblock(g, (place) -> {
            if (place != null &&
                    withinReach(g, g.getCurrentPlayer().getCurrentPlace(), place)) {
                ci.setRoadblock(place);
            }
            cb.run(null);
        });
    }

    private boolean withinReach(Game g, Place advance, Place place) {
        int reach = (Integer) g.getConfig("roadblock-reach");
        Place back = advance;
        if (advance == place) {
            return true;
        }
        for (int i = 0; i<reach; i++) {
            advance = advance.getNext();
            back = back.getPrev();
            if (advance == place || back == place) {
                return true;
            }
        }
        return false;
    }
}
