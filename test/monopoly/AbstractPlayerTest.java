package monopoly;

import monopoly.async.Callback;
import monopoly.async.MoneyChangeEvent;
import monopoly.place.Land;
import monopoly.place.Street;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class AbstractPlayerTest {
    private AbstractPlayer player;
    private AbstractPlayer anotherPlayer;
    private Street street;
    private Property prop;
    private Property anotherProp;
    private Game g;
    private Card card;
    private AbstractPlayer.PlaceInterface pi;
    private AbstractPlayer.CardInterface ci;

    private Game.State gameState;

    private boolean askWhetherToBuyPropertyCalled = false,
            askWhetherToUpgradePropertyCalled = false,
            askWhichCardToUseCalled = false,
            rollTheDiceCalled = false,
            bankruptTriggered = false,
            doNotUseCard = false;
    private Card cardUsed;
    private int moneyChangeAmount;

    private Callback<Object> cb;

    @Before
    public void setUp() {
        player = new AIPlayer() {
            @Override
            public void askWhichCardToUse(Game g, Callback<Card> cb) {
                askWhichCardToUseCalled = true;
                if (!doNotUseCard) {
                    cb.run(card);
                } else {
                    cb.run(null);
                }
            }

            @Override
            public void askWhetherToBuyProperty(Game g, Callback<Boolean> cb) {
                askWhetherToBuyPropertyCalled = true;
                cb.run(true);
            }

            @Override
            public void askWhetherToUpgradeProperty(Game g, Callback<Boolean> cb) {
                askWhetherToUpgradePropertyCalled = true;
                cb.run(true);
            }
        };

        anotherPlayer = new AIPlayer();
        street = new Street("");
        prop = new Land("Prop", 10, street) {};
        anotherProp = new Land("Prop2", 20, street) {};
        prop.next = prop.prev = anotherProp;
        anotherProp.next = anotherProp.prev = prop;

        g = new Game() {
            @Override
            public State getState() {
                return gameState;
            }

            @Override
            void rollTheDice() {
                rollTheDiceCalled = true;
            }

            @Override
            void useCard(Card card, Callback<Object> cb) {
                cardUsed = card;
            }

            @Override
            void triggerMoneyChange(MoneyChangeEvent event) {
                moneyChangeAmount = event.getAmount();
            }

            @Override
            void triggerBankrupt(AbstractPlayer player) {
                bankruptTriggered = true;
            }
        };

        card = new Card("Card") {
        };

        pi = new AbstractPlayer.PlaceInterface();
        ci = new AbstractPlayer.CardInterface();

        cb = (o) -> {};
    }

    @Test
    public synchronized void testName() {
        player.setName("foo");
        assertEquals("foo", player.getName());
    }

    @Test
    public synchronized void testGetProperties() {
        List<Property> properties = player.getProperties();
        properties.add(prop);
        assertThat(player.getProperties(), not(hasItem(prop)));
    }

    @Test
    public synchronized void testPossessions() {
        player.init(100, 20, 0, prop);
        assertEquals(120, player.getTotalPossessions());
        gameState = Game.State.TURN_LANDED;
        player.buyProperty(g, cb);
        assertEquals(120, player.getTotalPossessions());
        prop.mortgage();
    }

    @Test
    public synchronized void testStartTurn() {
        askWhichCardToUseCalled = false;
        rollTheDiceCalled = false;
        cardUsed = null;
        gameState = Game.State.TURN_STARTING;

        player.startTurn(g);
        assertFalse(askWhichCardToUseCalled);
        assertTrue(rollTheDiceCalled);
        assertNull(cardUsed);

        askWhichCardToUseCalled = false;
        rollTheDiceCalled = false;
        cardUsed = null;
        pi.addCard(player, g, card);
        doNotUseCard = false;
        player.startTurn(g);
        assertTrue(askWhichCardToUseCalled);
        assertFalse(rollTheDiceCalled);
        assertEquals(card, cardUsed);

        askWhichCardToUseCalled = false;
        rollTheDiceCalled = false;
        cardUsed = null;
        pi.addCard(player, g, card);
        doNotUseCard = true;
        player.startTurn(g);
        assertTrue(askWhichCardToUseCalled);
        assertTrue(rollTheDiceCalled);
        assertNull(cardUsed);
    }

    @Test
    public synchronized void testChangeCash() {
        moneyChangeAmount = 0;
        player.init(0, 0, 0, prop);
        pi.changeCash(player, g, 100, "");
        assertEquals(100, player.getCash());
        assertEquals(100, moneyChangeAmount);
        assertEquals(100, player.getTotalPossessions());
    }

    @Test
    public synchronized void testChangeDeposit() {
        moneyChangeAmount = 0;
        player.init(0, 0, 0, prop);
        pi.changeDeposit(player, g, 100, "");
        assertEquals(100, player.getDeposit());
        assertEquals(100, moneyChangeAmount);
        assertEquals(100, player.getTotalPossessions());
    }

    @Test
    public synchronized void testPay() {
        bankruptTriggered = false;
        moneyChangeAmount = 0;
        player.init(20, 20, 0, prop);
        anotherPlayer.init(0, 0, 0, prop);
        gameState = Game.State.TURN_LANDED;
        player.buyProperty(g, cb);
        assertEquals(10, player.getCash());
        assertFalse(bankruptTriggered);

        player.pay(g, anotherPlayer, 20, "", cb);
        assertEquals(0, player.getCash());
        assertEquals(10, player.getDeposit());
        assertEquals(20, anotherPlayer.getCash());
        assertFalse(bankruptTriggered);

        player.pay(g, null, 15, "", cb);
        assertEquals(5, player.getCash());
        assertEquals(0, player.getDeposit());
        assertEquals(0, player.getProperties().size());
        assertFalse(bankruptTriggered);

        player.pay(g, anotherPlayer, 10, "", cb);
        assertEquals(15, anotherPlayer.getCash());
        assertTrue(bankruptTriggered);

        prop.mortgage();
    }

    @Test
    public synchronized void testBuyProperty() {
        player.init(1000, 0, 0, prop);
        gameState = Game.State.TURN_LANDED;
        player.buyProperty(g, cb);

        List<Property> properties = player.getProperties();
        assertEquals(1, properties.size());
        assertThat(properties, hasItem(prop));

        gameState = Game.State.TURN_WALKING;
        player.startWalking(g, 1);
        assertEquals(anotherProp, player.getCurrentPlace());

        gameState = Game.State.TURN_LANDED;
        player.buyProperty(g, cb);
        properties = player.getProperties();
        assertEquals(2, properties.size());
        assertThat(properties, hasItem(prop));
        assertThat(properties, hasItem(anotherProp));

        prop.mortgage();
        anotherProp.mortgage();
    }

    @Test
    public synchronized void testUpgradeProperty() {
        player.init(100, 0, 0, prop);
        gameState = Game.State.TURN_LANDED;
        player.buyProperty(g, cb);

        assertEquals(1, prop.getLevel());
        moneyChangeAmount = 0;
        player.upgradeProperty(g, cb);
        assertEquals(2, prop.getLevel());
        assertEquals(85, player.getCash());
        assertEquals(-5, moneyChangeAmount);
    }
}