package monopoly;

import monopoly.async.CashChangeEvent;
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

    GameData(Config c) {
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
                onGameStart.trigger(this, null);
                startTurn();
            }
        }
    }

    private void startTurn() {
        synchronized (lock) {
            boolean notFirst = data.state == State.TURN_ENDING;
            if (data.state == State.STARTING || notFirst) {
                data.state = State.TURN_STARTING;
                onTurn.trigger(this, null);
                if (data.players.isNewCycle() && notFirst) {
                    onCycle.trigger(this, null);
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
            onGameOver.trigger(this, null);
        }
    }

    private static java.util.Map<String, Event<Object>> oEvents = new Hashtable<>();
    private static java.util.Map<String, Event<AbstractPlayer>> pEvents = new Hashtable<>();
    private static java.util.Map<String, Event<CashChangeEvent>> cEvents = new Hashtable<>();

    private static Event<Object> onGameStart = new Event<>(),
    onGameOver = new Event<>(),
    onTurn = new Event<>(),
    onCycle = new Event<>();
    private static Event<AbstractPlayer> onBankrupt = new Event<>();
    private static Event<CashChangeEvent> onCashChange = new Event<>();

    static {
        oEvents.put("gameStart", onGameStart);
        oEvents.put("gameOver", onGameOver);
        oEvents.put("turn", onTurn);
        oEvents.put("cycle", onCycle);
        pEvents.put("bankrupt", onBankrupt);
        cEvents.put("cashChange", onCashChange);
    }

    private static <T> void on(java.util.Map<String, Event<T>> events, String id, Callback<T> callback) throws Exception {
        Event<T> event = events.get(id);
        if (event == null) {
            throw new Exception("No such event: " + id);
        }
        event.addListener(callback);
    }

    public static void onO(String id, Callback<Object> callback) throws Exception {
        Game.<Object> on(oEvents, id, callback);
    }

    public static void onP(String id, Callback<AbstractPlayer> callback) throws Exception {
        Game.<AbstractPlayer> on(pEvents, id, callback);
    }

    public static void onC(String id, Callback<CashChangeEvent> callback) throws Exception {
        Game.<CashChangeEvent> on(cEvents, id, callback);
    }

    void triggerCashChange(CashChangeEvent event) {
        synchronized (lock) {
            if (data.state != State.OVER) {
                onCashChange.trigger(this, event);
            }
        }
    }

    void triggerBankrupt(AbstractPlayer player) {
        synchronized (lock) {
            if (data.state != State.OVER) {
                data.players.remove(player);
                onBankrupt.trigger(this, player);
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
