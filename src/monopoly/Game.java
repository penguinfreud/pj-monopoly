package monopoly;

import monopoly.util.*;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;


class GameData implements Serializable {
    final Config config;
    transient ResourceBundle messages;
    Map map;
    final Players players = new Players();
    boolean hadBankrupt = false;
    Game.State state = Game.State.OVER;
    final AbstractPlayer.PlaceInterface placeInterface = new AbstractPlayer.PlaceInterface();
    final AbstractPlayer.CardInterface cardInterface;
    final java.util.Map<Object, Object> storage = new Hashtable<>();


    GameData(Game g, Config config) {
        this.config = config;
        cardInterface = new AbstractPlayer.CardInterface(g);
        updateMessages();
    }

    void updateMessages() {
        Locale locale = Locale.forLanguageTag((String) config.get("locale"));
        messages = ResourceBundle.getBundle((String) config.get("bundle-name"), locale);
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        updateMessages();
    }
}

public class Game {
    public enum State {
        OVER, STARTING, TURN_STARTING, TURN_WALKING, TURN_LANDED
    }

    final SerializableObject lock = new SerializableObject();
    private static final SerializableObject staticLock = new SerializableObject();

    private static final List<Callback<Object>> _onGameInit = new CopyOnWriteArrayList<>();
    private static final List<Game> games = new CopyOnWriteArrayList<>();

    private GameData data;

    private static final Executor pool = new ThreadPoolExecutor(1, 5, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    private static final Config defaultConfig = new Config();

    static {
        defaultConfig.put("bundle-name", "messages");
        defaultConfig.put("locale", "zh-CN");
        defaultConfig.put("dice-sides", 6);
    }

    public static void putDefaultConfig(String key, Object value) {
        defaultConfig.put(key, value);
    }

    public Game() {
        this(null);
    }

    protected Game(Config c) {
        synchronized (staticLock) {
            if (c == null) {
                c = defaultConfig;
            } else {
                c.setBase(defaultConfig);
            }
            data = new GameData(this, new Config(c));
            triggerGameInit(this);
            games.add(this);
        }
    }

    public State getState() {
        return data.state;
    }

    @SuppressWarnings("unchecked")
    public <T> T getConfig(String key) {
        synchronized (lock) {
            return (T) data.config.get(key);
        }
    }

    public void putConfig(String key, Object value) {
        synchronized (lock) {
            data.config.put(key, value);
            if (key.equals("bundle-name") || key.equals("locale")) {
                data.updateMessages();
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
            System.err.println("Unknown key: " + key);
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

    private void endTurn() {
        synchronized (lock) {
            if (data.state == State.TURN_LANDED) {
                if (inEndTurn) {
                    tailRecursion = true;
                } else {
                    inEndTurn = true;
                    do {
                        tailRecursion = false;
                        if (!data.hadBankrupt) {
                            data.players.next();
                        }
                        startTurn();
                    } while (tailRecursion);
                    inEndTurn = false;
                }
            }
        }
    };

    private boolean inEndTurn = false;
    private boolean tailRecursion = false;

    private static final Callback<Object> turnCb = (g, o) -> {
        g.endTurn();
    };

    void rollTheDice() {
        int dice = ThreadLocalRandom.current().nextInt(getConfig("dice-sides")) + 1;
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
            data.players.getCurrentPlayer().getCurrentPlace().onLanded(this, data.placeInterface, turnCb);
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

    public static void onGameInit(Callback<Object> listener) {
        synchronized (staticLock) {
            _onGameInit.add(listener);
            for (Game game: games) {
                listener.run(game, null);
            }
        }
    }

    private static void triggerGameInit(Game g) {
        synchronized (staticLock) {
            for (Callback<Object> listener : _onGameInit) {
                listener.run(g, null);
            }
        }
    }

    private static final Event<Object> _onGameStart = new Event<>(),
            _onGameOver = new Event<>(),
            _onTurn = new Event<>(),
            _onLanded = new Event<>(),
            _onCycle = new Event<>();
    private static final Event<String> _onException = new Event<>();
    private static final Event<AbstractPlayer> _onBankrupt = new Event<>();
    public static final EventWrapper<Object> onGameStart = new EventWrapper<>(_onGameStart),
            onGameOver = new EventWrapper<>(_onGameOver),
            onTurn = new EventWrapper<>(_onTurn),
            onLanded = new EventWrapper<>(_onLanded),
            onCycle = new EventWrapper<>(_onCycle);
    public static final EventWrapper<String> onException = new EventWrapper<>(_onException);
    public static final EventWrapper<AbstractPlayer> onBankrupt = new EventWrapper<>(_onBankrupt);

    void triggerBankrupt(AbstractPlayer player) {
        if (data.state != State.OVER) {
            data.players.remove(player);
            data.hadBankrupt = true;
            _onBankrupt.trigger(this, player);
        }
    }

    public void triggerException(String key, Object ...args) {
        _onException.trigger(this, format(key, args));
    }

    @SuppressWarnings("unchecked")
    public final <T> T getStorage(Object key) {
        synchronized (lock) {
            return (T) data.storage.get(key);
        }
    }

    public final void store(Object key, Object value) {
        synchronized (lock) {
            data.storage.put(key, value);
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
