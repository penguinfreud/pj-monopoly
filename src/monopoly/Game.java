package monopoly;

import monopoly.event.Event;
import monopoly.event.Listener;

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

    private final Object lock = new Object();
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

    void startTurn() {
        synchronized (lock) {
            if (data.state == State.STARTING || data.state == State.TURN_ENDING) {
                data.state = State.TURN_STARTING;
                data._onTurn.trigger(null);
                if (data.players.isNewCycle() && data.state == State.TURN_ENDING) {
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

    void startWalking(int steps) {
        synchronized (lock) {
            if (data.state == State.TURN_STARTING) {
                data.state = State.TURN_WALKING;
                data.players.getCurrentPlayer().startWalking(this, steps);
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
            if (data.state != State.OVER) {
                data._onCashChange.trigger(event);
            }
        }
    }

    public void onBankrupt(Listener<AbstractPlayer> listener) {
        data._onBankrupt.addListener(listener);
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
