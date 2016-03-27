package monopoly;

public class StopTheGame extends Property {
    public StopTheGame() {
        super("StopTheGame", Integer.MAX_VALUE);
    }

    @Override
    public int getRent() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void onPassingBy(Game g) {
        g.getCurrentPlayer().payRent(g);
    }

    @Override
    public void onLanded(Game g) {
        g.getCurrentPlayer().payRent(g);
    }
}
