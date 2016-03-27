package monopoly;

import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class GameTest {
    private ArrayList<AbstractPlayer> players;
    private Map map;
    private AbstractPlayer firstPlayer, secondPlayer;
    private Game game;

    @Before
    public void setUp() throws Exception {
        map = new Map(0);

        players = new ArrayList<>();
        firstPlayer = new Player("player A");
        secondPlayer = new Player("player B");
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
    }

    //@Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        File tmp = File.createTempFile("game", null);
        FileOutputStream fos = new FileOutputStream(tmp);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(game);

        oos.close();
        fos.close();

        FileInputStream fis = new FileInputStream(tmp);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Game newGame = (Game) ois.readObject();

        assertEquals(game.getCurrentPlayer().getName(), newGame.getCurrentPlayer().getName());
        assertEquals("bar", newGame.getConfig("foo"));
    }
}