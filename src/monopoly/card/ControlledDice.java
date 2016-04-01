package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Card;
import monopoly.Game;
import monopoly.Place;
import monopoly.async.Callback;

class ControlledDice extends Card {
    static {
        Card.registerCard(new ControlledDice());
    }

    private ControlledDice() {
        super("ControlledDice");
    }

    public void use(Game g, AbstractPlayer.CardInterface ci, Callback<Object> cb) {
        AbstractPlayer player = g.getCurrentPlayer();
        player.askWhereToGo(g, (place) -> {
            if (place == null) {
                cb.run(null);
            } else {
                int steps = getDistanceTo(player, g, place);
                if (steps != 0) {
                    ci.walk(g, steps);
                } else {
                    cb.run(null);
                }
            }
        });
    }

    private int getDistanceTo(AbstractPlayer player, Game g, Place place) {
        Place cur = player.getCurrentPlace();
        int diceSides = (Integer) g.getConfig("dice-sides");
        for (int i = 0; i<diceSides; i++) {
            cur = player.isReversed()? cur.getPrev(): cur.getNext();
            if (cur == place) {
                return i + 1;
            }
        }
        return 0;
    }
}
