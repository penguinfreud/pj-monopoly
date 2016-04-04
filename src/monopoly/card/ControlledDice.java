package monopoly.card;

import monopoly.AbstractPlayer;
import monopoly.Game;
import monopoly.Place;
import monopoly.util.Consumer0;

public class ControlledDice extends Card {
    static {
        registerCard(new ControlledDice());
        Game.putDefaultConfig("controlled-dice-price", 5);
    }

    private ControlledDice() {
        super("ControlledDice");
    }

    public void use(Game g, AbstractPlayer.CardInterface ci, Consumer0 cb) {
        AbstractPlayer player = g.getCurrentPlayer();
        player.askForPlace(g, getName(), (place) -> {
            synchronized (ci.lock) {
                if (place == null) {
                    cb.run();
                } else {
                    int reach = g.getConfig("dice-sides");
                    int steps = Place.withPlayersReach(player, place, reach);
                    if (steps != 0) {
                        ci.walk(steps);
                    } else {
                        cb.run();
                    }
                }
            }
        });
    }
}
