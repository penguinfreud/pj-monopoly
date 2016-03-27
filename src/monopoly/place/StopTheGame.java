package monopoly.place;

import monopoly.Map;

public class StopTheGame extends monopoly.StopTheGame {
    static {
        Map.registerPlaceType("StopTheGame", (sc) -> new StopTheGame());
    }
}
