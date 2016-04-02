package monopoly.tui;

import monopoly.Game;
import monopoly.Map;
import monopoly.MapReader;
import monopoly.Place;

import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TUIMap extends Map {
    static {
        Map.registerMapReader("TUIMap", new MapReader() {
            @Override
            protected Map createMap() {
                return new TUIMap();
            }
        });
    }

    List<List<TUIPlace>> rows = new CopyOnWriteArrayList<>();

    @Override
    protected void addPlace(Place place) {
        super.addPlace(place);

        TUIPlace tuiPlace = (TUIPlace) place;
        int x = tuiPlace.getX(), y = tuiPlace.getY();
        while (rows.size() <= y) {
            rows.add(new CopyOnWriteArrayList<>());
        }
        List<TUIPlace> cols = rows.get(y);
        while (cols.size() <= x) {
            cols.add(null);
        }
        cols.set(x, tuiPlace);
    }

    void print(Game g, PrintStream out, boolean raw) {
        for (List<TUIPlace> row: rows) {
            for (TUIPlace place: row) {
                if (place == null) {
                    out.print("  ");
                } else {
                    out.print(place.print(g, raw));
                }
            }
            out.println();
        }
    }
}
