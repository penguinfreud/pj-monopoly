package monopoly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Game {
    private Config config;
    private Random random = new Random();
    private Map map;
    private ArrayList<AbstractPlayer> players;
    private int currentPlayerIndex;
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
        return players.get(currentPlayerIndex);
    }

    public void start() {
        if (started) return;
        Collections.shuffle(players);
        currentPlayerIndex = 0;
        started = true;
        beginTurn();
    }

    void beginTurn() {
        AbstractPlayer currentPlayer = getCurrentPlayer();
        currentPlayer.beginTurn(this);
    }

    public void rollTheDice() {
        int dice = random.nextInt(config.get("dice-sides").getInt());
    }

}
