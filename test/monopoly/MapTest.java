package monopoly;

import monopoly.place.Street;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class MapTest {
    private File tempFile;

    @Before
    public void setUp() throws IOException, ClassNotFoundException {
        Class.forName("monopoly.MapReader");
        Class.forName("monopoly.place.Street");
        tempFile = File.createTempFile("map", "txt");
        PrintStream ps = new PrintStream(new FileOutputStream(tempFile));
        ps.print("Map,foo,Street,myStreet,10");
        ps.close();
    }

    @Test
    public void testMapFromFile() throws Exception {
        Map map = Map.readMap(new FileInputStream(tempFile));
        assertEquals(1, map.size());
        Place p = map.getStartingPoint();
        assertThat(p, instanceOf(Street.class));
        assertEquals("myStreet", p.getName());
        assertEquals(10, ((Street) p).getPrice());
        assertEquals(p, p.prev);
        assertEquals(p, p.next);
    }

    @After
    public void tearDown() {
        tempFile.delete();
    }
}