package monopoly;

import monopoly.event.Event;
import monopoly.event.Listener;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

class GameData implements Serializable {
    Config config = new Config();
    monopoly.place.Map map;
    Players players = new Players();
    boolean started = false;

    Event<Object> _onGameOver = new Event<>(),
        _onTurn = new Event<>(),
        _onCycle = new Event<>();
    Event<Game.CashChangeEvent> _onCashChange = new Event<>();
    Event<AbstractPlayer> _onBankrupt = new Event<>();
}

public class Game {
    private final Object lock = new Object();
    private Random random = new Random();
    private GameData data = new GameData();

    public Object getConfig(String key) {
        synchronized (lock) {
            return data.config.configTable.get(key);
        }
    }

    public void putConfig(String key, Object value) {
        synchronized (lock) {
            if (!data.started) {
                data.config.configTable.put(key, value);
            }
        }
    }

    public monopoly.place.Map getMap() {
        return data.map;
    }

    public void setMap(monopoly.place.Map map) {
        synchronized (lock) {
            if (!data.started) {
                data.map = map;
            }
        }
    }

    public void setPlayers(List<AbstractPlayer> playersList) throws Exception {
        synchronized (lock) {
            if (!data.started) {
                data.players.set(playersList);
            }
        }
    }

    public AbstractPlayer getCurrentPlayer() {
        synchronized (lock) {
            return data.players.getCurrentPlayer();
        }
    }

    public void start() {
        synchronized (lock) {
            if (data.started) return;
            data.started = true;
            data.players.init(this);
            _onGameStart.trigger(this);
            beginTurn();
        }
    }

    void beginTurn() {
        synchronized (lock) {
            if (data.started) {
                data.players.getCurrentPlayer().beginTurn(this);
            }
        }
    }

    void endTurn() {
        synchronized (lock) {
            if (data.started) {
                data.players.next();
                beginTurn();
            }
        }
    }

    public void rollTheDice() {
        synchronized (lock) {
            if (data.started) {
                int dice = random.nextInt((Integer) getConfig("dice sides")) + 1;
                data.players.getCurrentPlayer().advance(this, dice);
            }
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

    private static Event<Game> _onGameStart = new Event<>();

    public static void onGameStart(Listener<Game> listener) {
        _onGameStart.addListener(listener);
    }

    public void onGameOver(Listener<Object> listener) {
        data._onGameOver.addListener(listener);
    }

    public void onTurn(Listener<Object> listener) {
        data._onTurn.addListener(listener);
    }

    public void onCycle(Listener<Object> listener) {
        data._onCycle.addListener(listener);
    }

    public void onCashChange(Listener<CashChangeEvent> listener) {
        data._onCashChange.addListener(listener);
    }

    void triggerCashChange(CashChangeEvent event) {
        synchronized (lock) {
            if (data.started) {
                data._onCashChange.trigger(event);
            }
        }
    }

    public void onBankrupt(Listener<AbstractPlayer> listener) {
        data._onBankrupt.addListener(listener);
    }

    void triggerBankrupt(AbstractPlayer player) {
        synchronized (lock) {
            if (data.started) {
                data.players.remove(player);
                data._onBankrupt.trigger(player);
                if (data.players.count() == 1) {
                    data._onGameOver.trigger(null);
                }
            }
        }
    }

    void readData(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        synchronized (lock) {
            data = (GameData) ois.readObject();
        }
    }

    void writeData(ObjectOutputStream oos) throws IOException {
        synchronized (lock) {
            oos.writeObject(data);
        }
    }
}
