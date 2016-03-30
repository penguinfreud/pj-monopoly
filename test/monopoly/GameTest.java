package monopoly;

import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class GameTest {
    private ArrayList<AbstractPlayer> players;
    private Map map;
    private AbstractPlayer firstPlayer, secondPlayer;
    private Game game;

    @Before
    public void setUp() throws Exception {
        Class.forName("monopoly.MapReader");
        Class.forName("monopoly.place.Land");
        Class.forName("monopoly.place.StopTheGame");
        map = Map.readMap(new FileInputStream("test_res/test.map"));

        players = new ArrayList<>();
        firstPlayer = new AIPlayer("player A");
        secondPlayer = new AIPlayer("player B");
        players.add(firstPlayer);
        players.add(secondPlayer);

        game = new Game();
        game.setMap(map);
        game.setPlayers(players);
        game.putConfig("foo", "bar");
    }

    @Test
    public void testSetUp() {
        assertEquals(map, game.getMap());
        assertTrue(game.getCurrentPlayer() == firstPlayer ||
            game.getCurrentPlayer() == secondPlayer);
        assertEquals("bar", game.getConfig("foo"));
        assertEquals("a", game.getMap().getStartingPoint().getName());

        game.start();
    }
}