package monopoly;

import monopoly.player.AIPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        Map map = Map.fromFile(new File("res/test.map"));

        List<AbstractPlayer> players = new ArrayList<>();
        players.add(new AIPlayer("player A"));
        players.add(new AIPlayer("player B"));

        Game game = new Game();
        game.setMap(map);
        game.setPlayers(players);
        game.start();
    }
}
