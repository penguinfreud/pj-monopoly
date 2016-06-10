package monopoly.extension;

import monopoly.Game;
import monopoly.util.Event0;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Map;

public final class GameCalendar {
    public static final Map<Game, Event0> onMonth = new Hashtable<>();
    private static final Map<Game, GameCalendar> calendars = new Hashtable<>();

    public static void enable(Game g) {
        if (calendars.get(g) == null) {
            onMonth.put(g, new Event0());
            calendars.put(g, new GameCalendar(g));
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
        return MessageFormat.format("date_format",
                String.format("%d", calendar.get(Calendar.YEAR)),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DATE));
    }
}