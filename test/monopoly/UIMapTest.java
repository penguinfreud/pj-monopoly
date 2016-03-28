package monopoly;

import monopoly.ui.UIPlace;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class UIMapTest {
    @Before
    public void setUp() throws IOException, ClassNotFoundException {
        Class.forName("monopoly.ui.UIMapReader");
        Class.forName("monopoly.place.Street");
    }

    @Test
    public void testMapFromFile() throws Exception {
        Map map = Map.readMap(new FileInputStream("ui.map"));
        assertEquals(1, map.size());
        Place p = map.getStartingPoint();
        assertThat(p, instanceOf(UIPlace.class));
        assertEquals(10, ((UIPlace) p).getX());
        assertEquals(10, ((UIPlace) p).getY());
        assertEquals("a", p.getName());
        assertEquals(15, p.asProperty().getPrice());
        assertEquals(p, p.prev);
        assertEquals(p, p.next);
    }
}
