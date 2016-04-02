package monopoly;

import monopoly.async.EventDispatcher;

import java.io.Serializable;
import java.text.MessageFormat;

class Calendar implements Serializable {
    private final java.util.Calendar calendar = java.util.Calendar.getInstance();
    private final EventDispatcher<Object> onMonth = new EventDispatcher<>();

    Calendar(Game g) {
        calendar.set(2014, java.util.Calendar.JANUARY, 1);
        g.registerOEvent("month", onMonth);

        g.onO("cycle", (_g, o) -> {
            int oldMonth = calendar.get(java.util.Calendar.MONTH);
            calendar.add(java.util.Calendar.DATE, 1);
            int newMoth = calendar.get(java.util.Calendar.MONTH);
            if (oldMonth < newMoth) {
                onMonth.trigger(_g, null);
            }
        });
    }

    String getDate(Game g) {
        return MessageFormat.format(g.getText("date_format"),
                String.format("%d", calendar.get(java.util.Calendar.YEAR)),
                calendar.get(java.util.Calendar.MONTH) + 1,
                calendar.get(java.util.Calendar.DATE));
    }
}
