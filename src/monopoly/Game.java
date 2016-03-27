package monopoly;

import java.util.ArrayList;

public class Game {
    private Map map;
    private ArrayList<AbstractPlayer> players;
    private AbstractPlayer currentPlayer;
    private boolean started = false;

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public void addPlayer(AbstractPlayer player) {
        this.players.add(player);
    }

    public void removePlayer(AbstractPlayer player) {
        this.players.remove(player);
    }

    public AbstractPlayer getCurrentPlayer() {
        return currentPlayer;
    }
}
