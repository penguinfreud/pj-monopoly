package monopoly;

import monopoly.event.Event;
import monopoly.event.Listener;

import java.util.*;

public class Game {
    private Config config;
    private Random random = new Random();
    private Map map;
    private Players players = new Players();
    private boolean started = false;

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        if (!started) {
            this.config = config;
        }
    }

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        if (!started) {
            this.map = map;
        }
    }

    public void setPlayers(List<AbstractPlayer> playersList) throws Exception {
        if (!started) {
            players.setPlayers(playersList);
        }
    }

    public AbstractPlayer getCurrentPlayer() {
        return players.getCurrentPlayer();
    }

    public void start() {
        if (started) return;
        started = true;
        players.initPlayers(this);
        _onGameStart.trigger(this, null);
        beginTurn();
    }

    void beginTurn() {
        if (started) {
            getCurrentPlayer().beginTurn(this);
        }
    }

    void endTurn() {
        if (started) {
            players.next();
            beginTurn();
        }
    }

    public void rollTheDice() {
        if (started) {

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

    public void onCashChange(Listener<CashChangeEvent> listener) {
        _onCashChange.addListener(listener);
    }

    public void triggerCashChange(CashChangeEvent event) {
        if (started) {
            _onCashChange.trigger(this, event);
        }
    }

    public void onBankrupt(Listener<AbstractPlayer> listener) {
        _onBankrupt.addListener(listener);
    }

    public void triggerBankrupt(AbstractPlayer player) {
        if (started) {
            players.removePlayer(player);
            _onBankrupt.trigger(this, player);
            if (players.count() == 1) {
                _onGameOver.trigger(this, null);
            }
        }
    }
}
