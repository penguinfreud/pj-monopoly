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

    Event<Object> _onGameStart = new Event<>(),
            _onGameOver = new Event<>();
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
                data.players.setPlayers(playersList);
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
            data.players.initPlayers(this);
            data._onGameStart.trigger(null);
            beginTurn();
        }
    }

    void beginTurn() {
        synchronized (lock) {
            if (data.started) {
                getCurrentPlayer().beginTurn(this);
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


    public void onGameStart(Listener<Object> listener) {
        data._onGameStart.addListener(listener);
    }

    public void onGameOver(Listener<Object> listener) {
        data._onGameOver.addListener(listener);
    }

    public void onCashChange(Listener<CashChangeEvent> listener) {
        data._onCashChange.addListener(listener);
    }

    public void triggerCashChange(CashChangeEvent event) {
        synchronized (lock) {
            if (data.started) {
                data._onCashChange.trigger(event);
            }
        }
    }

    public void onBankrupt(Listener<AbstractPlayer> listener) {
        data._onBankrupt.addListener(listener);
    }

    public void triggerBankrupt(AbstractPlayer player) {
        synchronized (lock) {
            if (data.started) {
                data.players.removePlayer(player);
                data._onBankrupt.trigger(player);
                if (data.players.count() == 1) {
                    data._onGameOver.trigger(null);
                }
            }
        }
    }

    public void readData(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        synchronized (lock) {
            data = (GameData) ois.readObject();
        }
    }

    public void writeData(ObjectOutputStream oos) throws IOException {
        synchronized (lock) {
            oos.writeObject(data);
        }
    }
}
