package monopoly;

import monopoly.async.Callback;
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
        player = new AbstractPlayer() {
            @Override
            public void askWhetherToBuyProperty(Game g, Callback<Boolean> cb) {
                cb.run(false);
            }

            @Override
            public void askWhetherToUpgradeProperty(Game g, Callback<Boolean> cb) {
                cb.run(false);
            }

            @Override
            public void askWhichPropertyToMortgage(Game g, Callback<Property> cb) {
                cb.run(getProperties().get(0));
            }

            @Override
            public void askWhichCardToUse(Game g, Callback<Card> cb) {
                cb.run(null);
            }

            @Override
            public void askHowMuchToDepositOrWithdraw(Game g, Callback<Integer> cb) {
                cb.run(0);
            }
            
            @Override
            public void askWhichPlaceToGo(Game g, Callback<Place> cb) {
                Place cur = getCurrentPlace();
                cb.run(isReversed()? cur.getPrev(): cur.getNext());
            }
        };

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