package monopoly;

import monopoly.gui.GUIPlace;
import monopoly.place.GameMap;
import monopoly.place.Place;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class GUIGameMapTest {
    @Before
    public void setUp() throws IOException, ClassNotFoundException {
        Class.forName("monopoly.gui.GUIGameMapReader");
        Class.forName("monopoly.gui.GUIPlace");
        Class.forName("monopoly.place.Land");
    }

    @Test
    public void testMapFromFile() throws Exception {
        GameMap map = GameMap.readMap(new FileInputStream("test_res/gui.map"));
        assertEquals(1, map.size());
        Place p = map.getStartingPoint();
        assertThat(p, instanceOf(GUIPlace.class));
        assertEquals(10, ((GUIPlace) p).getX());
        assertEquals(10, ((GUIPlace) p).getY());
        assertEquals("a", p.getName());
        assertEquals(15, p.asProperty().getPrice());
        assertEquals(p, p.getPrev());
        assertEquals(p, p.getNext());
    }
}
