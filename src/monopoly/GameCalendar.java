package monopoly;

import monopoly.util.Event;
import monopoly.util.EventWrapper;
import monopoly.util.SerializableObject;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Calendar;

final class GameCalendar implements Serializable {
    private static final SerializableObject key = new SerializableObject();
    private final Calendar calendar = Calendar.getInstance();
    private static final Event<Object> _onMonth = new Event<>();
    public static final EventWrapper<Object> onMonth = new EventWrapper<>(_onMonth);

    static {
        Game.onGameInit((g, o) -> {
            g.store(key, new GameCalendar());
            Game.onCycle.addListener(g, (_g, _o) -> _g.<GameCalendar>getStorage(key).increment(_g));
        });
    }

    private void increment(Game g) {
        int oldMonth = calendar.get(java.util.Calendar.MONTH);
        calendar.add(java.util.Calendar.DATE, 1);
        int newMoth = calendar.get(java.util.Calendar.MONTH);
        if (oldMonth < newMoth) {
            _onMonth.trigger(g, null);
        }
    }

    static String getDate(Game g) {
        Calendar calendar = g.<GameCalendar>getStorage(key).calendar;
        return MessageFormat.format(g.getText("date_format"),
                String.format("%d", calendar.get(java.util.Calendar.YEAR)),
                calendar.get(java.util.Calendar.MONTH) + 1,
                calendar.get(java.util.Calendar.DATE));
    }
}