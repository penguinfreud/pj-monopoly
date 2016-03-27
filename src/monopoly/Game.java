package monopoly;

import monopoly.event.Event;
import monopoly.event.Listener;

import java.io.Serializable;
import java.util.*;

public class Game implements Serializable {
    private transient final Object lock = new Object();

    private Config config = new Config();
    private Random random = new Random();
    private Map map;
    private Players players = new Players();
    private boolean started = false;

    public Object getConfig(String key) {
        synchronized (lock) {
            return config.configTable.get(key);
        }
    }

    public void putConfig(String key, Object value) {
        synchronized (lock) {
            if (!started) {
                config.configTable.put(key, value);
            }
        }
    }

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        synchronized (lock) {
            if (!started) {
                this.map = map;
            }
        }
    }

    public void setPlayers(List<AbstractPlayer> playersList) throws Exception {
        synchronized (lock) {
            if (!started) {
                players.setPlayers(playersList);
            }
        }
    }

    public AbstractPlayer getCurrentPlayer() {
        synchronized (lock) {
            return players.getCurrentPlayer();
        }
    }

    public void start() {
        synchronized (lock) {
            if (started) return;
            started = true;
            players.initPlayers(this);
            _onGameStart.trigger(this, null);
            beginTurn();
        }
    }

    void beginTurn() {
        synchronized (lock) {
            if (started) {
                getCurrentPlayer().beginTurn(this);
            }
        }
    }

    void endTurn() {
        synchronized (lock) {
            if (started) {
                players.next();
                beginTurn();
            }
        }
    }

    public void rollTheDice() {
        synchronized (lock) {
            if (started) {

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

    private Event<Object> _onGameStart = new Event<>(),
        _onGameOver = new Event<>();
    private Event<CashChangeEvent> _onCashChange = new Event<>();
    private Event<AbstractPlayer> _onBankrupt = new Event<>();

    public void onGameStart(Listener<Object> listener) {
        _onGameStart.addListener(listener);
    }

    public void onGameOver(Listener<Object> listener) {
        _onGameOver.addListener(listener);
    }

    public void onCashChange(Listener<CashChangeEvent> listener) {
        _onCashChange.addListener(listener);
    }

    public void triggerCashChange(CashChangeEvent event) {
        synchronized (lock) {
            if (started) {
                _onCashChange.trigger(this, event);
            }
        }
    }

    public void onBankrupt(Listener<AbstractPlayer> listener) {
        _onBankrupt.addListener(listener);
    }

    public void triggerBankrupt(AbstractPlayer player) {
        synchronized (lock) {
            if (started) {
                players.removePlayer(player);
                _onBankrupt.trigger(this, player);
                if (players.count() == 1) {
                    _onGameOver.trigger(this, null);
                }
            }
        }
    }
}
