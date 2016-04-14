package monopoly.place;

public class Empty extends Place {
    static {
        GameMap.registerPlaceReader("Empty", (r, sc) -> new Empty());
    }

    private Empty() {
        super("Empty");
    }
}
