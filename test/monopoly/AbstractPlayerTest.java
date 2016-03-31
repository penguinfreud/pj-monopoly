package monopoly;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class AbstractPlayerTest {
    private AbstractPlayer player;
    private AbstractPlayer anotherPlayer;
    private Property prop;
    private Property anotherProp;
    private Game g;

    @Before
    public void setUp() {
        player = new AIPlayer();
        anotherPlayer = new AIPlayer();
        prop = new Property("Prop", 10) {};
        anotherProp = new Property("Prop2", 20) {};
        g = new Game() {};
    }

    @Test
    public void testGetProperties() {
        List<Property> properties = player.getProperties();
        properties.add(prop);
        assertThat(player.getProperties(), not(hasItem(prop)));
    }

    @Test
    public void testPossessions() {
        player.initCash(100);
        player.initDeposit(20);
        assertEquals(120, player.getTotalPossessions());
        player.initPlace(prop);
        player.buyProperty(g, (o) -> {});
        assertEquals(120, player.getTotalPossessions());
    }
}