package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Card;
import monopoly.Game;
import monopoly.Place;
import monopoly.async.Callback;

class ControlledDice extends Card {
    static {
        Card.registerCard(new ControlledDice());
        Game.putDefaultConfig("card-controlleddice-price", 5);
    }

    private ControlledDice() {
        super("ControlledDice");
    }

    public void use(Game g, AbstractPlayer.CardInterface ci, Callback<Object> cb) {
        AbstractPlayer player = g.getCurrentPlayer();
        player.askForPlace(g, getName(), (_g, place) -> {
            if (place == null) {
                cb.run(_g, null);
            } else {
                int reach = (Integer) _g.getConfig("dice-sides");
                int steps = Place.withPlayersReach(player, place, reach);
                if (steps != 0) {
                    ci.walk(_g, steps);
                } else {
                    cb.run(_g, null);
                }
            }
        });
    }
}
