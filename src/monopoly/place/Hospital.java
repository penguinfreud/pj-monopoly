package monopoly.place;

import monopoly.Game;
import monopoly.IPlayer;

import java.util.Hashtable;
import java.util.Map;

public class Hospital extends Place {
    private static final Map<IPlayer, Integer> turnsToStay = new Hashtable<>();

    static {
        GameMap.registerPlaceReader("Hospital", (r, sc) -> new Hospital());
        Game.onInit.addListener(game -> {
            game.onTurn.addListener(() -> {
                IPlayer player = game.getCurrentPlayer();
                Integer i = turnsToStay.get(player);
                if (i != null && i > 0) {
                    turnsToStay.put(player, turnsToStay.get(player) - 1);
                    game.startWalking(0);
                }
            });

            game.onStartWalking.addListener(steps -> {
                IPlayer player = game.getCurrentPlayer();
                Integer i = turnsToStay.get(player);
                if (i != null && i == 0) {
                    turnsToStay.remove(player);
                    if (!player.isReversed()) {
                        player.reverse();
                    }
                }
            });
        });
    }

    private Hospital() {
        super("Hospital");
    }

    public static void accept(IPlayer player) {
        if (!turnsToStay.containsKey(player)) {
            turnsToStay.put(player, 2);
        }
    }
}
