package monopoly.card;

import monopoly.*;
import monopoly.util.Consumer0;

public class ControlledDice extends Card {
    static {
        registerCard(new ControlledDice());
        Game.putDefaultConfig("controlled-dice-price", 5);
    }

    private ControlledDice() {
        super("ControlledDice");
    }

    @Override
    public void use(Game g, Consumer0 cb) {
        IPlayer player = g.getCurrentPlayer();
        ((Cards.IPlayerWithCards) player).askForTargetPlace(getName(), g.sync(place -> {
            if (place == null) {
                cb.run();
            } else {
                int reach = g.getConfig("dice-sides");
                int steps = Place.withinPlayersReach(player, place, reach);
                if (steps != 0) {
                    g.startWalking(steps);
                } else {
                    cb.run();
                }
            }
        }));
    }
}
