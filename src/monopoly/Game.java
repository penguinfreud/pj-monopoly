package monopoly;

import monopoly.async.DelegateEventDispatcher;
import monopoly.async.EventDispatcher;
import monopoly.async.Callback;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


class GameData implements Serializable {
    final Config config;
    transient ResourceBundle messages;
    Map map;
    final GameCalendar calendar = new GameCalendar();
    final Players players = new Players();
    boolean hadBankrupt = false;
    Game.State state = Game.State.OVER;
    final AbstractPlayer.PlaceInterface placeInterface = new AbstractPlayer.PlaceInterface();
    final AbstractPlayer.CardInterface cardInterface = new AbstractPlayer.CardInterface();

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

    GameCalendar getInternalCalendar() {
        return data.calendar;
    }

    public String getDate() {
        return GameCalendar.getDate(this);
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
            _onTurn.trigger(this, null);
            if (data.players.isNewCycle() && notFirst) {
                _onCycle.trigger(this, null);
            }
            data.players.getCurrentPlayer().startTurn(this);
        }
    }

    private final Callback<Object> endTurn = (g, o) -> {
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
            _onLanded.trigger(this, null);
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
            _onGameOver.trigger(this, null);
        }
    }

    private static final EventDispatcher<Object> _onGameOver = new EventDispatcher<>(),
        _onTurn = new EventDispatcher<>(),
        _onLanded = new EventDispatcher<>(),
        _onCycle = new EventDispatcher<>();
    private static final EventDispatcher<Exception> _onException = new EventDispatcher<>();
    private static final EventDispatcher<AbstractPlayer> _onBankrupt = new EventDispatcher<>();
    public static final DelegateEventDispatcher<Object> onGameOver = new DelegateEventDispatcher<>(_onGameOver),
        onTurn = new DelegateEventDispatcher<>(_onTurn),
        onLanded = new DelegateEventDispatcher<>(_onLanded),
        onCycle = new DelegateEventDispatcher<>(_onCycle);
    public static final DelegateEventDispatcher<Exception> onException = new DelegateEventDispatcher<>(_onException);
    public static final DelegateEventDispatcher<AbstractPlayer> onBankrupt = new DelegateEventDispatcher<>(_onBankrupt);

    void triggerBankrupt(AbstractPlayer player) {
        if (data.state != State.OVER) {
            data.players.remove(player);
            data.hadBankrupt = true;
            _onBankrupt.trigger(this, player);
        }
    }

    public void triggerException(Exception e) {
        _onException.trigger(this, e);
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

    private static final EventDispatcher<Object> _onGameStart = new EventDispatcher<>();

    public static final DelegateEventDispatcher<Object> onGameStart = new DelegateEventDispatcher<>(_onGameStart);
}
