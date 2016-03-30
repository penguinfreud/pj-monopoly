package monopoly;

import monopoly.async.Event;
import monopoly.async.Callback;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;


class GameData implements Serializable {
    Config config;
    Map map;
    Players players = new Players();
    Game.State state = Game.State.OVER;

    Event<Object> _onGameOver = new Event<>(),
        _onTurn = new Event<>(),
        _onCycle = new Event<>();
    Event<Game.CashChangeEvent> _onCashChange = new Event<>();
    Event<AbstractPlayer> _onBankrupt = new Event<>();

    public GameData(Config c) {
        config = c;
    }
}

public class Game {
    public enum State {
        OVER, STARTING, TURN_STARTING, TURN_WALKING, TURN_LANDED, TURN_ENDING
    }

    final Object lock = new Object();
    private Random random = new Random();
    private GameData data;

    public Game() {
        data = new GameData(new Config());
    }

    protected Game(Config c) {
        data = new GameData(c);
    }

    public State getState() {
        synchronized (lock) {
            return data.state;
        }
    }

    public Object getConfig(String key) {
        synchronized (lock) {
            return data.config.configTable.get(key);
        }
    }

    public void putConfig(String key, Object value) {
        synchronized (lock) {
            if (data.state == State.OVER) {
                data.config.configTable.put(key, value);
            }
        }
    }

    public Map getMap() {
        return data.map;
    }

    public void setMap(Map map) {
        synchronized (lock) {
            if (data.state == State.OVER) {
                data.map = map;
            }
        }
    }

    public void setPlayers(List<AbstractPlayer> playersList) throws Exception {
        synchronized (lock) {
            if (data.state == State.OVER) {
                data.players.set(playersList);
            }
        }
    }

    public List<AbstractPlayer> getPlayers() {
        synchronized (lock) {
            return data.players.getPlayers();
        }
    }

    public AbstractPlayer getCurrentPlayer() {
        synchronized (lock) {
            return data.players.getCurrentPlayer();
        }
    }

    public void start() {
        synchronized (lock) {
            if (data.state == State.OVER) {
                data.state = State.STARTING;
                data.players.init(this);
                _onGameStart.trigger(this);
                startTurn();
            }
        }
    }

    private void startTurn() {
        synchronized (lock) {
            boolean notFirst = data.state == State.TURN_ENDING;
            if (data.state == State.STARTING || notFirst) {
                data.state = State.TURN_STARTING;
                data._onTurn.trigger(null);
                if (data.players.isNewCycle() && notFirst) {
                    data._onCycle.trigger(null);
                }
                data.players.getCurrentPlayer().startTurn(this);
            }
        }
    }

    void endTurn() {
        synchronized (lock) {
            if (data.state == State.TURN_LANDED) {
                data.state = State.TURN_ENDING;
                data.players.next();
                startTurn();
            }
        }
    }

    void rollTheDice() {
        synchronized (lock) {
            int dice = random.nextInt((Integer) getConfig("dice sides")) + 1;
            startWalking(dice);
        }
    }

    public void startWalking(int steps) {
        synchronized (lock) {
            if (data.state == State.TURN_STARTING) {
                data.state = State.TURN_WALKING;
                data.players.getCurrentPlayer().startWalking(this, steps);
            }
        }
    }

    public void stay() {
        synchronized (lock) {
            if (data.state == State.TURN_STARTING) {
                data.state = State.TURN_LANDED;
                endTurn();
            }
        }
    }

    void endWalking() {
        synchronized (lock) {
            if (data.state == State.TURN_WALKING || data.state == State.TURN_STARTING) {
                data.state = State.TURN_LANDED;
                data.players.getCurrentPlayer().getCurrentPlace().onLanded(this);
            }
        }
    }

    private void endGame() {
        if (data.state != State.OVER) {
            data.state = State.OVER;
            data._onGameOver.trigger(null);
        }
    }

    public static class CashChangeEvent {
        private AbstractPlayer player;
        private int amount;

        public CashChangeEvent(AbstractPlayer player, int amount) {
            this.player = player;
            this.amount = amount;
        }

        public AbstractPlayer getPlayer() {
            return player;
        }

        public int getAmount() {
            return amount;
        }
    }

    private static final Event<Game> _onGameStart = new Event<>();

    public static void onGameStart(Callback<Game> callback) {
        _onGameStart.addListener(callback);
    }

    public void onGameOver(Callback<Object> callback) {
        data._onGameOver.addListener(callback);
    }

    public void onTurn(Callback<Object> callback) {
        data._onTurn.addListener(callback);
    }

    public void onCycle(Callback<Object> callback) {
        data._onCycle.addListener(callback);
    }

    public void onCashChange(Callback<CashChangeEvent> callback) {
        data._onCashChange.addListener(callback);
    }

    void triggerCashChange(CashChangeEvent event) {
        synchronized (lock) {
            if (data.state != State.OVER) {
                data._onCashChange.trigger(event);
            }
        }
    }

    public void onBankrupt(Callback<AbstractPlayer> callback) {
        data._onBankrupt.addListener(callback);
    }

    void triggerBankrupt(AbstractPlayer player) {
        synchronized (lock) {
            if (data.state != State.OVER) {
                data.players.remove(player);
                data._onBankrupt.trigger(player);
                if (data.players.count() == 1) {
                    endGame();
                }
            }
        }
    }

    protected void readData(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        synchronized (lock) {
            data = (GameData) ois.readObject();
        }
    }

    protected void writeData(ObjectOutputStream oos) throws IOException {
        synchronized (lock) {
            oos.writeObject(data);
        }
    }
}
