package monopoly;

import monopoly.place.Land;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class GameMapTest {
    private File tempFile;

    @Before
    public void setUp() throws IOException, ClassNotFoundException {
        Class.forName("monopoly.GameMapReader");
        Class.forName("monopoly.place.Land");
        tempFile = File.createTempFile("map", "txt");
        PrintStream ps = new PrintStream(new FileOutputStream(tempFile));
        ps.print("GameMap,foo,Land,myStreet,10,s");
        ps.close();
    }

    @Test
    public void testMapFromFile() throws Exception {
        GameMap map = GameMap.readMap(new FileInputStream(tempFile));
        assertEquals(1, map.size());
        Place p = map.getStartingPoint();
        assertThat(p, instanceOf(Land.class));
        assertEquals("myStreet", p.getName());
        assertEquals(10, ((Land) p).getPrice());
        assertEquals(p, p.prev);
        assertEquals(p, p.next);
    }

    @After
    public void tearDown() {
        tempFile.delete();
    }
}