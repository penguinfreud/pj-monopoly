package monopoly;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.Assert.assertEquals;

public class GameSerializationTest {
    private List<AbstractPlayer> players;
    private Map map;
    private AbstractPlayer firstPlayer, secondPlayer;
    private Game game;
    private File tempFile;

    @Before
    public void setUp() throws Exception {
        Class.forName("monopoly.MapReader");
        Class.forName("monopoly.place.Land");
        Class.forName("monopoly.place.StopTheGame");
        map = Map.readMap(new FileInputStream("test_res/test.map"));

        players = new CopyOnWriteArrayList<>();
        firstPlayer = new AIPlayer("player A");
        secondPlayer = new AIPlayer("player B");
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
        assertEquals("a", game.getMap().getStartingPoint().getName());

        ois.close();
        fis.close();
    }

    @After
    public void tearDown() {
        tempFile.delete();
    }
}
