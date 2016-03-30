package monopoly;

import monopoly.async.Event;
import monopoly.async.Callback;

import java.util.Calendar;

public final class Date {
    static {
        Game.onGameStart((g) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(2016, Calendar.JANUARY, 1);
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

    private static final Event<Game> _onMonth = new Event<>();

    public void onMonth(Callback<Game> callback) {
        _onMonth.addListener(callback);
    }
}
