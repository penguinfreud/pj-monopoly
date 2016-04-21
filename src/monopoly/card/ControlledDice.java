package monopoly.card;

import monopoly.Card;
import monopoly.Game;
import monopoly.IPlayer;
import monopoly.Cards;
import monopoly.place.Place;
import monopoly.util.Consumer1;

public class ControlledDice extends Card {
    private static final Card instance = new ControlledDice();
    static {
        Game.putDefaultConfig("controlled-dice-price", 5);
    }

    private ControlledDice() {
        super("ControlledDice");
    }

    public static void enable(Game g) {
        Cards.enableCard(g, instance);
    }

    @Override
    public void use(Game g, Consumer1<Boolean> cb) {
        IPlayer player = g.getCurrentPlayer();
        ((Cards.IPlayerWithCards) player).askForTargetPlace(getName(), place -> {
            synchronized (g.lock) {
                if (place == null) {
                    cb.run(false);
                } else {
                    int reach = g.getConfig("dice-sides");
                    int steps = Place.withinPlayersReach(player, place, reach);
                    if (steps != 0) {
                        g.startWalking(steps);
                    } else {
                        cb.run(false);
                    }
                }
            }
        });
    }
}
