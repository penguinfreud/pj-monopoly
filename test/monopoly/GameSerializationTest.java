package monopoly;

import monopoly.place.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class GameSerializationTest {
    private ArrayList<AbstractPlayer> players;
    private Map map;
    private AbstractPlayer firstPlayer, secondPlayer;
    private Game game;
    private File tempFile;

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

        tempFile = File.createTempFile("game", null);
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        FileOutputStream fos = new FileOutputStream(tempFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);

        game.writeData(oos);

        oos.close();
        fos.close();

        FileInputStream fis = new FileInputStream(tempFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        game.readData(ois);

        assertEquals(game.getCurrentPlayer().getName(), game.getCurrentPlayer().getName());
        assertEquals("bar", game.getConfig("foo"));

        ois.close();
        fis.close();
    }

    @After
    public void tearDown() {
        tempFile.delete();
    }
}
