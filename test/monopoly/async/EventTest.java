package monopoly.async;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class EventTest {
    private Event<Integer> event;
    private int x;

    @Before
    public void setUp() {
        event = new Event<>();
        x = 0;
    }

    @Test
    public void testEvent() {
        event.addListener((i) -> x = i);

        event.trigger(13);
        assertEquals(13, x);
    }
}