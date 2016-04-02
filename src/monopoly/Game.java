package monopoly;

import monopoly.async.MoneyChangeEvent;
import monopoly.async.EventDispatcher;
import monopoly.async.Callback;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


class GameData implements Serializable {
    Config config;
    transient ResourceBundle messages;
    Map map;
    Calendar calendar;
    Bank bank;
    Players players = new Players();
    boolean hadBankrupt = false;
    Game.State state = Game.State.OVER;
    AbstractPlayer.PlaceInterface placeInterface = new AbstractPlayer.PlaceInterface();
    AbstractPlayer.CardInterface cardInterface = new AbstractPlayer.CardInterface();


    java.util.Map<String, EventDispatcher<Object>> oEvents = new Hashtable<>();
    java.util.Map<String, EventDispatcher<Exception>> eEvents = new Hashtable();
    java.util.Map<String, EventDispatcher<AbstractPlayer>> pEvents = new Hashtable<>();
    java.util.Map<String, EventDispatcher<MoneyChangeEvent>> mEvents = new Hashtable<>();

    EventDispatcher<Object> onGameOver = new EventDispatcher<>(),
            onTurn = new EventDispatcher<>(),
            onLanded = new EventDispatcher<>(),
            onCycle = new EventDispatcher<>();
    EventDispatcher<Exception> onException = new EventDispatcher<>();
    EventDispatcher<AbstractPlayer> onBankrupt = new EventDispatcher<>();
    EventDispatcher<MoneyChangeEvent> onMoneyChange = new EventDispatcher<>();

    GameData(Config c) {
        Config def = new Config();
        defaultConfig(def);
        config = c;
        c.setBase(def);
    }

    private void defaultConfig(Config config) {
        config.put("bundle-name", "messages");
        config.put("locale", "zh-CN");

        config.put("dice-sides", 6);

        config.put("init-cash", 2000);
        config.put("init-deposit", 2000);
        config.put("init-coupons", 0);

        config.put("property-max-level", 6);

        config.put("news-award-min", 100);
        config.put("news-award-max", 200);

        config.put("coupon-award-min", 1);
        config.put("coupon-award-max", 10);

        config.put("card-controlleddice-price", 5);
        config.put("card-reversecard-price", 3);
        config.put("card-roadblock-price", 3);
        config.put("card-staycard-price", 3);

        config.put("bank-max-transfer", 100000);

        config.put("roadblock-reach", 8);
    }

    void init(Game g) {
        oEvents.put("gameOver", onGameOver);
        oEvents.put("turn", onTurn);
        oEvents.put("landed", onLanded);
        oEvents.put("cycle", onCycle);
        eEvents.put("exception", onException);
        pEvents.put("bankrupt", onBankrupt);
        mEvents.put("moneyChange", onMoneyChange);
        calendar = new Calendar(g);
        bank = new Bank(g);
        Locale locale = Locale.forLanguageTag((String) config.get("locale"));
        messages = ResourceBundle.getBundle((String) config.get("bundle-name"), locale);
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        Locale locale = Locale.forLanguageTag((String) config.get("locale"));
        messages = ResourceBundle.getBundle((String) config.get("bundle-name"), locale);
    }
}

public class Game {
    public enum State {
        OVER, STARTING, TURN_STARTING, TURN_WALKING, TURN_LANDED
    }

    final Object lock = new Object();
    private GameData data;

    public Game() {
        this(null);
    }

    protected Game(Config c) {
        data = new GameData(new Config(c));
        data.init(this);
    }

    public State getState() {
        return data.state;
    }

    public Object getConfig(String key) {
        synchronized (lock) {
            return data.config.get(key);
        }
    }

    public void putConfig(String key, Object value) {
        synchronized (lock) {
            data.config.put(key, value);
            if (key.equals("bundle-name") || key.equals("locale")) {
                Locale locale = Locale.forLanguageTag((String) data.config.get("locale"));
                data.messages = ResourceBundle.getBundle((String) data.config .get("bundle-name"), locale);
            }
        }
    }

    public String getText(String key) {
        try {
            return new String(data.messages.getString(key).getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        } catch(MissingResourceException e) {
            return "";
        }
    }

    public String format(String key, Object ...args) {
        return MessageFormat.format(getText(key), args);
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
        return data.players.getPlayers();
    }

    public AbstractPlayer getCurrentPlayer() {
        return data.players.getCurrentPlayer();
    }

    public String getDate() {
        return data.calendar.getDate(this);
    }

    public void start() {
        synchronized (lock) {
            if (data.state == State.OVER) {
                data.state = State.STARTING;
                data.players.init(this);
                _onGameStart.trigger(this, null);
                startTurn();
            }
        }
    }

    private void startTurn() {
        if (data.players.count() <= 1) {
            endGame();
        }
        boolean notFirst = data.state == State.TURN_LANDED;
        if (data.state == State.STARTING || notFirst) {
            data.state = State.TURN_STARTING;
            data.hadBankrupt = false;
            data.onTurn.trigger(this, null);
            if (data.players.isNewCycle() && notFirst) {
                data.onCycle.trigger(this, null);
            }
            data.players.getCurrentPlayer().startTurn(this);
        }
    }

    private Callback<Object> endTurn = (g, o) -> {
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
        int dice = ThreadLocalRandom.current().nextInt((Integer) getConfig("dice-sides")) + 1;
        if (data.players.count() <= 1) {
            endGame();
        }
        startWalking(dice);
    }

    void startWalking(int steps) {
        if (data.state == State.TURN_STARTING) {
            data.state = State.TURN_WALKING;
            data.players.getCurrentPlayer().startWalking(this, steps);
        }
    }

    void stay() {
        if (data.state == State.TURN_STARTING) {
            data.state = State.TURN_LANDED;
            data.players.next();
            startTurn();
        }
    }

    void endWalking() {
        if (data.state == State.TURN_WALKING || data.state == State.TURN_STARTING) {
            data.state = State.TURN_LANDED;
            data.onLanded.trigger(this, null);
            data.players.getCurrentPlayer().getCurrentPlace().onLanded(this, data.placeInterface, endTurn);
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
            data.onGameOver.trigger(this, null);
        }
    }

    public void onO(String id, Callback<Object> callback) {
        data.oEvents.get(id).addListener(callback);
    }

    public void onP(String id, Callback<AbstractPlayer> callback) {
        data.pEvents.get(id).addListener(callback);
    }

    public void onM(String id, Callback<MoneyChangeEvent> callback) {
        data.mEvents.get(id).addListener(callback);
    }

    void registerOEvent(String id, EventDispatcher<Object> eventDispatcher) {
        data.oEvents.put(id, eventDispatcher);
    }

    void registerPEvent(String id, EventDispatcher<AbstractPlayer> eventDispatcher) {
        data.pEvents.put(id, eventDispatcher);
    }

    void registerMEvent(String id, EventDispatcher<MoneyChangeEvent> eventDispatcher) {
        data.mEvents.put(id, eventDispatcher);
    }

    void triggerMoneyChange(MoneyChangeEvent event) {
        if (data.state != State.OVER) {
            data.onMoneyChange.trigger(this, event);
        }
    }

    void triggerBankrupt(AbstractPlayer player) {
        if (data.state != State.OVER) {
            data.players.remove(player);
            data.hadBankrupt = true;
            data.onBankrupt.trigger(this, player);
        }
    }

    public void triggerException(Exception e) {
        data.onException.trigger(this, e);
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

    private static EventDispatcher<Game> _onGameStart = new EventDispatcher<>();

    public static void onGameStart(Callback<Game> listener) {
        _onGameStart.addListener(listener);
    }
}
