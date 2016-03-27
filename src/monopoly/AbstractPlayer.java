package monopoly;

public abstract class AbstractPlayer {
    public void beginTurn(Game g) {
        g.rollTheDice();
    }
}
