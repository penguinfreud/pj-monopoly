package monopoly;

import monopoly.async.MoneyChangeEvent;
import monopoly.async.Event;
import monopoly.async.Callback;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


class GameData implements Serializable {
    Config config;
    Map map;
    Calendar calendar;
    Bank bank;
    Players players = new Players();
    boolean hadBankrupt = false;
    Game.State state = Game.State.OVER;
    AbstractPlayer.PlaceInterface placeInterface = new AbstractPlayer.PlaceInterface();
    AbstractPlayer.CardInterface cardInterface = new AbstractPlayer.CardInterface();


    java.util.Map<String, Event<Object>> oEvents = new Hashtable<>();
    java.util.Map<String, Event<AbstractPlayer>> pEvents = new Hashtable<>();
    java.util.Map<String, Event<MoneyChangeEvent>> mEvents = new Hashtable<>();

    Event<Object> onGameStart = new Event<>(),
            onGameOver = new Event<>(),
            onTurn = new Event<>(),
            onCycle = new Event<>();
    Event<AbstractPlayer> onBankrupt = new Event<>();
    Event<MoneyChangeEvent> onMoneyChange = new Event<>();

    GameData(Game g, Config c) {
        config = c;
    }

    void init(Game g) {
        oEvents.put("gameStart", onGameStart);
        oEvents.put("gameOver", onGameOver);
        oEvents.put("turn", onTurn);
        oEvents.put("cycle", onCycle);
        pEvents.put("bankrupt", onBankrupt);
        mEvents.put("cashChange", onMoneyChange);
        calendar = new Calendar(g);
    }
}

public class Game {
    public enum State {
        OVER, STARTING, TURN_STARTING, TURN_WALKING, TURN_LANDED
    }

    final Object lock = new Object();
    private GameData data;

    public Game() {
        data = new GameData(this, new Config());
        data.init(this);
    }

    protected Game(Config c) {
        data = new GameData(this, c);
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

    public String getDate() {
        return data.calendar.getDate();
    }

    public void start() {
        synchronized (lock) {
            if (data.state == State.OVER) {
                data.state = State.STARTING;
                data.players.init(this);
                data.onGameStart.trigger(null);
                startTurn();
            }
        }
    }

    private void startTurn() {
        synchronized (lock) {
            if (data.players.count() <= 1) {
                endGame();
            }
            boolean notFirst = data.state == State.TURN_LANDED;
            if (data.state == State.STARTING || notFirst) {
                data.state = State.TURN_STARTING;
                data.hadBankrupt = false;
                data.onTurn.trigger(null);
                if (data.players.isNewCycle() && notFirst) {
                    data.onCycle.trigger(null);
                }
                data.players.getCurrentPlayer().startTurn(this);
            }
        }
    }

    private Callback<Object> endTurn = (o) -> {
        synchronized (lock) {
            if (data.state == State.TURN_LANDED) {
                if (!data.hadBankrupt) {
                    data.players.next();
                }
                startTurn();
            }
        }
    };

    void rollTheDice() {
        synchronized (lock) {
            int dice = ThreadLocalRandom.current().nextInt((Integer) getConfig("dice-sides")) + 1;
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
                data.players.next();
                startTurn();
            }
        }
    }

    void endWalking() {
        synchronized (lock) {
            if (data.state == State.TURN_WALKING || data.state == State.TURN_STARTING) {
                data.state = State.TURN_LANDED;
                data.players.getCurrentPlayer().getCurrentPlace().onLanded(this, data.placeInterface, endTurn);
            }
        }
    }

    void passBy(Place place, Callback<Object> cb) {
        place.onPassingBy(this, data.placeInterface, cb);
    }

    void useCard(Card card, Callback<Object> cb) {
        card.use(this, data.cardInterface, cb);
    }

    private void endGame() {
        if (data.state != State.OVER) {
            data.state = State.OVER;
            data.onGameOver.trigger(null);
        }
    }

    public void onO(String id, Callback<Object> callback) {
        data.oEvents.get(id).addListener(callback);
    }

    public void onP(String id, Callback<AbstractPlayer> callback) {
        data.pEvents.get(id).addListener(callback);
    }

    public void onC(String id, Callback<MoneyChangeEvent> callback) {
        data.mEvents.get(id).addListener(callback);
    }

    void registerOEvent(String id, Event<Object> event) {
        data.oEvents.put(id, event);
    }

    void registerPEvent(String id, Event<AbstractPlayer> event) {
        data.pEvents.put(id, event);
    }

    void registerCEvent(String id, Event<MoneyChangeEvent> event) {
        data.mEvents.put(id, event);
    }

    void triggerMoneyChange(MoneyChangeEvent event) {
        synchronized (lock) {
            if (data.state != State.OVER) {
                data.onMoneyChange.trigger(event);
            }
        }
    }

    void triggerBankrupt(AbstractPlayer player) {
        synchronized (lock) {
            if (data.state != State.OVER) {
                data.players.remove(player);
                data.hadBankrupt = true;
                data.onBankrupt.trigger(player);
            }
        }
    }

    private static java.util.Map<Object, Object> storage = new Hashtable<>();

    public Object getData(Object key) {
        return storage.get(key);
    }

    public void putData(Object key, Object val) {
        storage.put(key, val);
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
