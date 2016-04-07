package monopoly;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class GameTest {
    private GameMap map;
    private AbstractPlayer firstPlayer, secondPlayer;
    private Game game;

    @Before
    public void setUp() throws Exception {
        Class.forName("monopoly.GameMapReader");
        Class.forName("monopoly.place.Land");
        Class.forName("monopoly.place.Trap");
        map = GameMap.readMap(GameTest.class.getResourceAsStream("/test.map"));

        List<AbstractPlayer> players = new ArrayList<>();
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