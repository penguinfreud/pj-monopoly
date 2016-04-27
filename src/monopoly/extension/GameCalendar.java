package monopoly.extension;

import monopoly.Game;
import monopoly.util.*;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Calendar;

public final class GameCalendar implements Serializable {
    public static final Parasite<Game, Event0> onMonth = new Parasite<>("GameCalendar.onMonth");
    private static final Parasite<Game, GameCalendar> calendars = new Parasite<>("GameCalendar");

    public static void enable(Game g) {
        if (calendars.get(g) == null) {
            onMonth.set(g, new Event0());
            calendars.set(g, new GameCalendar(g));
        }
    }

    public static boolean isEnabled(Game g) {
        return calendars.get(g) != null;
    }

    private final Game game;
    private final Calendar calendar = Calendar.getInstance();

    private GameCalendar(Game g) {
        game = g;
        g.onCycle.addListener(this::increment);
    }

    private void increment() {
        int oldMonth = calendar.get(Calendar.MONTH);
        calendar.add(Calendar.DATE, 1);
        int newMoth = calendar.get(Calendar.MONTH);
        if (oldMonth < newMoth) {
            onMonth.get(game).trigger();
        }
    }

    public static int getField(Game g, int field) {
        return calendars.get(g).calendar.get(field);
    }

    public static String getDate(Game g) {
        Calendar calendar = calendars.get(g).calendar;
        return MessageFormat.format(g.getText("date_format"),
                String.format("%d", calendar.get(Calendar.YEAR)),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DATE));
    }
}