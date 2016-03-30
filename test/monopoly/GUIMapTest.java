package monopoly;

import monopoly.gui.GUIPlace;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class GUIMapTest {
    @Before
    public void setUp() throws IOException, ClassNotFoundException {
        Class.forName("monopoly.gui.GUIMapReader");
        Class.forName("monopoly.gui.GUIPlace");
        Class.forName("monopoly.place.Land");
    }

    @Test
    public void testMapFromFile() throws Exception {
        Map map = Map.readMap(new FileInputStream("test_res/gui.map"));
        assertEquals(1, map.size());
        Place p = map.getStartingPoint();
        assertThat(p, instanceOf(GUIPlace.class));
        assertEquals(10, ((GUIPlace) p).getX());
        assertEquals(10, ((GUIPlace) p).getY());
        assertEquals("a", p.getName());
        assertEquals(15, p.asProperty().getPrice());
        assertEquals(p, p.prev);
        assertEquals(p, p.next);
    }
}
