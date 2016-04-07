package monopoly;

import org.junit.Test;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;

public class GameCalendarTest {
    private MyGame game = new MyGame();

    public GameCalendarTest() throws Exception {
        Class.forName("monopoly.GameMapReader");
        Class.forName("monopoly.place.Land");
        Class.forName("monopoly.GameCalendar");
        GameMap map = GameMap.readMap(GameTest.class.getResourceAsStream("/test.map"));
        game.setMap(map);
        List<AbstractPlayer> players = new ArrayList<>();
        players.add(new AIPlayer("player A"));
        players.add(new AIPlayer("player B"));
        game.setPlayers(players);
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
        Game.onTurn.addListener(game, () -> {
            assertEquals(formatDate(calendar), GameCalendar.getDate(game));
        });

        Game.onCycle.addListener(game, () -> {
            calendar.add(Calendar.DATE, 1);
        });

        game.forwardUntil = 70;
        game.start();
    }
}