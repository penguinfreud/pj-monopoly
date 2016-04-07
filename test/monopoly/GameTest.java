package monopoly;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class GameTest {
    private static class Player extends BasePlayer implements Properties.IPlayerWithProperties {
        Player(String name) {
            super(name);
        }
    }

    private GameMap map;
    private List<IPlayer> players = new ArrayList<>();
    private IPlayer playerA = new Player("player A"),
            playerB = playerA = new Player("player B"),
            playerC = playerA = new Player("player C");

    private MyGame game;

    public GameTest() throws Exception {
        Class.forName("monopoly.GameMapReader");
        Class.forName("monopoly.place.Land");
        map = GameMap.readMap(GameTest.class.getResourceAsStream("/test.map"));

        players.add(playerA);
        players.add(playerB);
    }

    @Before
    public void setUp() throws Exception {
        game = new MyGame();
        game.setMap(map);
        game.setPlayers(players);
    }

    @Test
    public void testConfig() {
        Game.putDefaultConfig("foo", "bar");
        assertEquals("bar", game.getConfig("foo"));
        game.putConfig("foo", 2);
        assertEquals((Integer)2, game.getConfig("foo"));

        int []original = {1, 2, 3};
        game.putConfig("hello", original);
        int[] arr = game.getConfig("hello");
        assertArrayEquals(arr, original);
    }

    @Test
    public void testGetState() {
        assertEquals(Game.State.OVER, game.getState());
        Game.onGameStart.addListener(game, () -> assertEquals(Game.State.STARTING, game.getState()));
        Game.onTurn.addListener(game, () -> assertEquals(Game.State.TURN_STARTING, game.getState()));
        Game.onCycle.addListener(game, () -> assertEquals(Game.State.TURN_STARTING, game.getState()));
        Game.onGameOver.addListener(game, () -> assertEquals(Game.State.OVER, game.getState()));
        Game.onLanded.addListener(game, () -> assertEquals(Game.State.TURN_LANDED, game.getState()));
        game.start();
    }

    @Test
    public void testGetMap() {
        assertEquals(map, game.getMap());
    }

    @Test
    public void testPlayers() throws Exception {
        assertEquals(2, game.getPlayers().size());
        players.add(playerC);
        assertEquals(2, game.getPlayers().size());
        assertThat(game.getCurrentPlayer(), anyOf(equalTo(playerA), equalTo(playerB)));

        game.setPlayers(players);
        game.start();

        List<IPlayer> _players = new ArrayList<>();
        _players.add(game.getCurrentPlayer());
        game.startWalking(1);
        _players.add(game.getCurrentPlayer());
        game.startWalking(1);
        _players.add(game.getCurrentPlayer());

        assertThat(_players, hasItem(playerA));
        assertThat(_players, hasItem(playerB));
        assertThat(_players, hasItem(playerC));
    }

    @Test
    public void testStart() throws Exception {
        game.putConfig("init-cash", 100);
        game.putConfig("init-deposit", 200);
        game.start();
        assertEquals(100, playerA.getCash());
        assertEquals(100, playerB.getCash());
        assertEquals(200, playerA.getDeposit());
        assertEquals(200, playerB.getDeposit());
    }

    @Test
    public void testWalking() throws Exception {
        game.putConfig("shuffle-players", false);
        game.setPlayers(players);
        game.start();
        assertEquals("a", playerA.getCurrentPlace().getName());
        assertEquals("a", playerB.getCurrentPlace().getName());
        game.startWalking(1);
        assertEquals("b", playerA.getCurrentPlace().getName());
        assertEquals("a", playerB.getCurrentPlace().getName());
        game.startWalking(2);
        assertEquals("b", playerA.getCurrentPlace().getName());
        assertEquals("c", playerB.getCurrentPlace().getName());
        game.startWalking(5);
        assertEquals("g", playerA.getCurrentPlace().getName());
        assertEquals("c", playerB.getCurrentPlace().getName());
    }

    @Test
    public void testTriggerBankrupt() throws Exception {
        players.add(playerC);
        game.setPlayers(players);
        List<IPlayer> bankrupted = new ArrayList<>();
        Game.onBankrupt.addListener(game, player -> bankrupted.add(player));
        game.forwardUntil = -1;
        game.start();
        assertEquals(2, bankrupted.size());
        assertEquals(1, game.getPlayers().size());
        assertThat(bankrupted, not(hasItem(game.getCurrentPlayer())));
    }
}