package monopoly;

import monopoly.place.GameMap;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class GameTest {
    private static class Player extends BasePlayer implements Properties.IPlayerWithProperties {
        Player(String name, Game g) {
            super(name, g);
        }
    }

    private GameMap map;
    private List<IPlayer> players = new ArrayList<>();
    private IPlayer playerA, playerB, playerC;

    private MyGame game;

    public GameTest() throws Exception {
        Class.forName("monopoly.place.GameMapReader");
        Class.forName("monopoly.place.Land");
        Class.forName("monopoly.place.Trap");
        map = GameMap.readMap(GameTest.class.getResourceAsStream("/test.map"));
    }

    @Before
    public void setUp() throws Exception {
        game = new MyGame();
        game.setMap(map);
        playerA = new Player("player A", game);
        playerB = new Player("player B", game);
        playerC = new Player("player C", game);
        players.add(playerA);
        players.add(playerB);
        game.setPlayers(players);
        Properties.enable(game);
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
        game.onGameStart.addListener(() -> assertEquals(Game.State.STARTING, game.getState()));
        game.onTurn.addListener(() -> assertEquals(Game.State.TURN_STARTING, game.getState()));
        game.onCycle.addListener(() -> assertEquals(Game.State.TURN_STARTING, game.getState()));
        game.onGameOver.addListener(() -> assertEquals(Game.State.OVER, game.getState()));
        game.onLanded.addListener(() -> assertEquals(Game.State.TURN_LANDED, game.getState()));
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
        game.putConfig("enable-cash", 100.0);
        game.putConfig("enable-deposit", 200.0);
        game.start();
        assertEquals(100, playerA.getCash(), 1e-8);
        assertEquals(100, playerB.getCash(), 1e-8);
        assertEquals(200, playerA.getDeposit(), 1e-8);
        assertEquals(200, playerB.getDeposit(), 1e-8);
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
        game.onBankrupt.addListener(bankrupted::add);
        game.forwardUntil = -1;
        game.start();
        assertEquals(2, bankrupted.size());
        assertEquals(1, game.getPlayers().size());
        assertThat(bankrupted, not(hasItem(game.getCurrentPlayer())));
    }
}