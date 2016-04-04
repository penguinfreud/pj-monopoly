package monopoly;

import monopoly.util.*;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Calendar;

final class GameCalendar implements Serializable {
    private final Game game;
    private final Calendar calendar = Calendar.getInstance();
    private static final Parasite<Game, Event0> _onMonth = new Parasite<>(Game::onInit, Event0::New);
    private static final Parasite<Game, GameCalendar> calendars = new Parasite<>(Game::onInit, GameCalendar::new);
    public static final EventWrapper<Game, Consumer0> onMonth = new EventWrapper<>(_onMonth);

    private GameCalendar(Game g) {
        game = g;
        Game.onCycle.addListener(g, this::increment);
    }

    private void increment() {
        int oldMonth = calendar.get(java.util.Calendar.MONTH);
        calendar.add(java.util.Calendar.DATE, 1);
        int newMoth = calendar.get(java.util.Calendar.MONTH);
        if (oldMonth < newMoth) {
            _onMonth.get(game).trigger();
        }
    }

    static String getDate(Game g) {
        Calendar calendar = calendars.get(g).calendar;
        return MessageFormat.format(g.getText("date_format"),
                String.format("%d", calendar.get(java.util.Calendar.YEAR)),
                calendar.get(java.util.Calendar.MONTH) + 1,
                calendar.get(java.util.Calendar.DATE));
    }
}