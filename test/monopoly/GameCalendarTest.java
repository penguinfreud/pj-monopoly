package monopoly;

import monopoly.extension.GameCalendar;
import monopoly.place.GameMap;
import org.junit.Test;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;

public class GameCalendarTest {
    private static class Player extends BasePlayer implements Properties.IPlayerWithProperties {
        Player(String name, Game g) {
            super(name, g);
        }
    }

    private MyGame game = new MyGame();

    public GameCalendarTest() throws Exception {
        Class.forName("monopoly.place.GameMapReader");
        Class.forName("monopoly.place.Land");
        Class.forName("monopoly.place.Trap");
        GameMap map = GameMap.readMap(GameTest.class.getResourceAsStream("/test.map"));
        game.setMap(map);
        List<IPlayer> players = new ArrayList<>();
        players.add(new Player("player A", game));
        players.add(new Player("player A", game));
        game.setPlayers(players);
        GameCalendar.init(game);
        Properties.init(game);
    }

    private String formatDate(Calendar calendar) {
        return MessageFormat.format(game.getText("date_format"),
                String.format("%d", calendar.get(Calendar.YEAR)),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DATE));
    }

    @Test
    public void testGetDate() throws Exception {
        Calendar calendar = Calendar.getInstance();
        game.onTurn.addListener(() -> assertEquals(formatDate(calendar), GameCalendar.getDate(game)));
        game.onCycle.addListener(() -> calendar.add(Calendar.DATE, 1));
        game.forwardUntil = 70;
        game.start();
    }
}