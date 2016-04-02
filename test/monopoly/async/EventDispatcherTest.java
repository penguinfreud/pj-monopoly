package monopoly.async;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class EventDispatcherTest {
    private EventDispatcher<Integer> eventDispatcher;
    private int x;

    @Before
    public void setUp() {
        eventDispatcher = new EventDispatcher<>();
        x = 0;
    }

    @Test
    public void testEvent() {
        eventDispatcher.addListener((g, i) -> x = i);

        eventDispatcher.trigger(null, 13);
        assertEquals(13, x);
    }
}