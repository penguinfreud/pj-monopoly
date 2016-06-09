package monopoly;

import monopoly.extension.GameCalendar;
import monopoly.place.GameMap;
import monopoly.place.GameMapReader;
import org.junit.Test;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;

public class GameCalendarTest {
    private static class Player extends BasePlayer implements Properties.IPlayerWithProperties {
        Player(String name, Game g) {
            super(g);
            setName(name);
        }
    }

    private MyGame game = new MyGame();

    public GameCalendarTest() throws Exception {
        Class.forName("monopoly.place.GameMapReader");
        Class.forName("monopoly.place.Land");
        Class.forName("monopoly.place.Trap");
        GameMap map = GameMap.readMap(GameTest.class.getResourceAsStream("/test.map"), new GameMapReader());
        game.setMap(map);
        game.addPlayer(new Player("player A", game));
        game.addPlayer(new Player("player A", game));
        GameCalendar.enable(game);
        Properties.enable(game);
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