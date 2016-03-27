package monopoly;

import monopoly.event.Event;
import monopoly.event.Listener;

import java.util.Calendar;

public final class Date {
    static {
        Game.onGameStart((g) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(2016, 0, 1);
            g.onCycle((obj) -> {
                int oldMonth = calendar.get(Calendar.MONTH);
                calendar.add(Calendar.DATE, 1);
                int newMonth = calendar.get(Calendar.MONTH);
                if (newMonth > oldMonth) {
                    Date._onMonth.trigger(g);
                }
            });
        });
    }

    private static Event<Game> _onMonth = new Event<>();

    public void onMonth(Listener<Game> listener) {
        _onMonth.addListener(listener);
    }
}
