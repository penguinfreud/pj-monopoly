package monopoly;

import monopoly.place.Land;
import monopoly.place.Street;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.Assert.assertEquals;

public class GameSerializationTest {
    private Game game;
    private File tempFile;
    private int turn = 0;
    private AbstractPlayer player, player2, currentPlayer, anotherPlayer;

    private static class DummyLand extends Land {
        protected DummyLand(String name, int price, Street street) {
            super(name, price, street);
        }
    }

    public GameSerializationTest() throws Exception {
        Class.forName("monopoly.MapReader");
        Class.forName("monopoly.place.Empty");
        Class.forName("monopoly.place.Land");
        Class.forName("monopoly.place.Bank");
        Class.forName("monopoly.place.News");
        Class.forName("monopoly.place.CouponSite");
        Class.forName("monopoly.place.CardSite");
        Class.forName("monopoly.place.CardShop");
        Class.forName("monopoly.place.Trap");
        Class.forName("monopoly.card.BuyLandCard");
        Class.forName("monopoly.card.ControlledDice");
        Class.forName("monopoly.card.ReverseCard");
        Class.forName("monopoly.card.Roadblock");
        Class.forName("monopoly.card.StayCard");
        Class.forName("monopoly.card.TaxCard");
        Class.forName("monopoly.card.EqualWealthCard");
        Class.forName("monopoly.card.GodOfLandCard");
        Class.forName("monopoly.card.TeardownCard");
        Class.forName("monopoly.card.RobCard");
        Class.forName("monopoly.card.GodOfLuckCard");

        List<AbstractPlayer> players = new CopyOnWriteArrayList<>();
        player = new AIPlayer("player A");
        player2 = new AIPlayer("player B");
        players.add(player);
        players.add(player2);

        game = new Game() {
            @Override
            void rollTheDice() {
                if (turn == 0) {
                    turn++;
                    startWalking(1);
                } else if (turn == 1) {
                    turn++;
                    startWalking(2);
                }
            }
        };

        AbstractPlayer.PlaceInterface pi = new AbstractPlayer.PlaceInterface();
        for (Card card: Card.getCards()) {
            pi.addCard(player, game, card);
        }

        Map map = new Map();
        map.addPlace(Map.getPlaceReader("Empty").read(null, null));
        Street street = new Street("dummy street");
        map.addPlace(new DummyLand("dummy land 1", 0, street));
        map.addPlace(new DummyLand("dummy land 2", 0, street));

        game.setMap(map);
        game.setPlayers(players);
        game.putConfig("foo", "bar");

        game.start();

        currentPlayer = game.getCurrentPlayer();
        anotherPlayer = currentPlayer == player? player2: player;
        tempFile = File.createTempFile("game", null);
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        FileOutputStream fos = new FileOutputStream(tempFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);

        game.writeData(oos);

        oos.close();
        fos.close();

        FileInputStream fis = new FileInputStream(tempFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        game.readData(ois);

        AbstractPlayer currentPlayer2, anotherPlayer2;
        currentPlayer2 = game.getCurrentPlayer();
        List<AbstractPlayer> players = game.getPlayers();
        anotherPlayer2 = players.get(0) == currentPlayer2? players.get(1): players.get(0);

        assertEquals(currentPlayer.getName(), currentPlayer2.getName());
        assertEquals(anotherPlayer.getName(), anotherPlayer2.getName());
        assertEquals("bar", game.getConfig("foo"));
        Place start = game.getMap().getStartingPoint();
        assertEquals("Empty", start.getName());

        start = start.getNext();
        assertEquals("dummy land 1", start.getName());
        assertEquals(start, currentPlayer2.getCurrentPlace());

        start = start.getNext();
        assertEquals("dummy land 2", start.getName());
        assertEquals(start, anotherPlayer2.getCurrentPlace());

        assertEquals(currentPlayer.getCards().size(), currentPlayer2.getCards().size());
        assertEquals(anotherPlayer.getCards().size(), anotherPlayer2.getCards().size());
        assertEquals(currentPlayer.getProperties().size(), currentPlayer2.getProperties().size());
        assertEquals(anotherPlayer.getProperties().size(), anotherPlayer2.getProperties().size());

        ois.close();
        fis.close();
    }

    @After
    public void tearDown() {
        if (tempFile != null) {
            tempFile.delete();
        }
    }
}
