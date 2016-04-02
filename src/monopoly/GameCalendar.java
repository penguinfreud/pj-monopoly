package monopoly;

import monopoly.async.DelegateEventDispatcher;
import monopoly.async.EventDispatcher;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Calendar;

class GameCalendar implements Serializable {
    private final Calendar calendar = Calendar.getInstance();

    private static final EventDispatcher<Object> _onMonth = new EventDispatcher<>();
    public static final DelegateEventDispatcher<Object> onMonth = new DelegateEventDispatcher<>(_onMonth);

    static {
        Game.onCycle.addListener((g, o) -> {
            Calendar calendar = g.getInternalCalendar().calendar;
            int oldMonth = calendar.get(java.util.Calendar.MONTH);
            calendar.add(java.util.Calendar.DATE, 1);
            int newMoth = calendar.get(java.util.Calendar.MONTH);
            if (oldMonth < newMoth) {
                _onMonth.trigger(g, null);
            }
        });
    }

    {
        calendar.set(2014, java.util.Calendar.JANUARY, 1);
    }

    static String getDate(Game g) {
        Calendar calendar = g.getInternalCalendar().calendar;
        return MessageFormat.format(g.getText("date_format"),
                String.format("%d", calendar.get(java.util.Calendar.YEAR)),
                calendar.get(java.util.Calendar.MONTH) + 1,
                calendar.get(java.util.Calendar.DATE));
    }
}
