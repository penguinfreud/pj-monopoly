package monopoly;

import monopoly.event.Listener;

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

    private Listener beginTurnCb = new Listener<Action>() {
        public void run(Action action) {

        }
    };

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
        started = true;
        Collections.shuffle(players);
        currentPlayerIndex = 0;

        for (AbstractPlayer player: players) {
            player.initPlace(map.getStartingPoint());
            player.initCash(config.get("init cash").getInt());
            player.initDeposit(config.get("init deposit").getInt());
        }

        beginTurn();
    }

    void beginTurn() {
        AbstractPlayer currentPlayer = getCurrentPlayer();
        currentPlayer.beginTurn(this, beginTurnCb);
    }
}
