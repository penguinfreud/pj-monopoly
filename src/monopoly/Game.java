package monopoly;

import monopoly.util.*;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Game implements Serializable, Host {
    private static final Logger logger = Logger.getLogger(Game.class.getName());
    public static final String WRONG_STATE = "wrong state";
    private static final SerializableObject staticLock = new SerializableObject();

    private static final List<Consumer1<Game>> _onGameInit = new CopyOnWriteArrayList<>();
    private static final List<Game> games = new CopyOnWriteArrayList<>();
    private static final Config defaultConfig = new Config();

    static {
        defaultConfig.put("bundle-name", "messages");
        defaultConfig.put("locale", "zh-CN");
        defaultConfig.put("dice-sides", 6);
    }

    enum State {
        OVER, STARTING, TURN_STARTING, TURN_WALKING, TURN_LANDED
    }
    
    private State state = State.OVER;
    private final Config config;
    private transient ResourceBundle messages;
    private Map map;
    private final Players players = new Players();
    private boolean hadBankrupt = false;
    private final java.util.Map<Object, Object> storage = new Hashtable<>();

    public static void putDefaultConfig(String key, Object value) {
        defaultConfig.put(key, value);
    }


    final SerializableObject lock = new SerializableObject();

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
            config = new Config(c);
            
            updateMessages();
            triggerGameInit(this);
            games.add(this);
        }
    }
    
    private void updateMessages() {
        Locale locale = Locale.forLanguageTag((String) config.get("locale"));
        messages = ResourceBundle.getBundle((String) config.get("bundle-name"), locale);
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        updateMessages();
    }

    public final State getState() {
        return state;
    }

    @SuppressWarnings("unchecked")
    public final <T> T getConfig(String key) {
        synchronized (lock) {
            return (T) config.get(key);
        }
    }

    protected final void putConfig(String key, Object value) {
        synchronized (lock) {
            config.put(key, value);
            if (key.equals("bundle-name") || key.equals("locale")) {
                updateMessages();
            }
        }
    }

    public final String getText(String key) {
        try {
            return new String(messages.getString(key).getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        } catch(MissingResourceException e) {
            //logger.log(Level.SEVERE, "Unknown key: " + key);
            return key;
        }
    }

    public final String format(String key, Object ...args) {
        return MessageFormat.format(getText(key), args);
    }

    public final Map getMap() {
        return map;
    }

    public final void setMap(Map map) {
        synchronized (lock) {
            if (state == State.OVER) {
                this.map = map;
            } else {
                logger.log(Level.WARNING, WRONG_STATE);
            }
        }
    }

    public final void setPlayers(List<AbstractPlayer> playersList) throws Exception {
        synchronized (lock) {
            if (state == State.OVER) {
                for (AbstractPlayer player: playersList) {
                    player.setGame(this);
                }
                players.set(playersList);
            } else {
                logger.log(Level.WARNING, WRONG_STATE);
            }
        }
    }

    public final List<AbstractPlayer> getPlayers() {
        return players.getPlayers();
    }

    public final AbstractPlayer getCurrentPlayer() {
        return players.getCurrentPlayer();
    }

    public final String getDate() {
        return GameCalendar.getDate(this);
    }

    public final void start() {
        synchronized (lock) {
            if (state == State.OVER) {
                state = State.STARTING;
                players.init(this);
                _onGameStart.get(this).trigger();
                startTurn();
            } else {
                logger.log(Level.WARNING, WRONG_STATE);
            }
        }
    }

    private void startTurn() {
        if (players.count() <= 1) {
            endGame();
        } else {
            boolean notFirst = state == State.TURN_LANDED;
            if (state == State.STARTING || notFirst) {
                state = State.TURN_STARTING;
                hadBankrupt = false;
                _onTurn.get(this).trigger();
                if (players.isNewCycle() && notFirst) {
                    _onCycle.get(this).trigger();
                }
                players.getCurrentPlayer().startTurn(this::startWalking);
            } else {
                logger.log(Level.WARNING, WRONG_STATE);
            }
        }
    }

    private void endTurn() {
        synchronized (lock) {
            if (state == State.TURN_LANDED) {
                if (inEndTurn) {
                    tailRecursion = true;
                } else {
                    inEndTurn = true;
                    do {
                        tailRecursion = false;
                        if (!hadBankrupt) {
                            players.next();
                        }
                        startTurn();
                    } while (tailRecursion);
                    inEndTurn = false;
                }
            } else {
                logger.log(Level.WARNING, WRONG_STATE);
            }
        }
    }

    private boolean inEndTurn = false;
    private boolean tailRecursion = false;

    private void startWalking() {
        int dice = ThreadLocalRandom.current().nextInt(getConfig("dice-sides")) + 1;
        startWalking(dice);
    }

    void startWalking(int steps) {
        if (state == State.TURN_STARTING) {
            state = State.TURN_WALKING;
            if (players.count() <= 1) {
                endGame();
            } else {
                if (steps == 0) {
                    endWalking();
                } else {
                    players.getCurrentPlayer().startWalking(steps);
                }
            }
        } else {
            logger.log(Level.WARNING, WRONG_STATE);
        }
    }

    void endWalking() {
        if (state == State.TURN_WALKING) {
            state = State.TURN_LANDED;
            _onLanded.get(this).trigger();
            players.getCurrentPlayer().onLanded(this::endTurn);
        } else {
            logger.log(Level.WARNING, WRONG_STATE);
        }
    }

    private void endGame() {
        if (state != State.OVER) {
            state = State.OVER;
            _onGameOver.get(this).trigger();
        } else {
            logger.log(Level.WARNING, WRONG_STATE);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <T> T getParasite(Object key) {
        synchronized (lock) {
            return (T) storage.get(key);
        }
    }

    @Override
    public final void setParasite(Object key, Object value) {
        synchronized (lock) {
            storage.put(key, value);
        }
    }

    public static void onInit(Consumer1<Game> listener) {
        synchronized (staticLock) {
            _onGameInit.add(listener);
            games.stream().forEach(listener::run);
        }
    }

    private static void triggerGameInit(Game g) {
        synchronized (staticLock) {
            for (Consumer1<Game> listener : _onGameInit) {
                listener.run(g);
            }
        }
    }

    private static final Parasite<Game, Event0> _onGameStart = new Parasite<>(Game::onInit, Event0::New),
            _onGameOver = new Parasite<>(Game::onInit, Event0::New),
            _onTurn = new Parasite<>(Game::onInit, Event0::New),
            _onLanded = new Parasite<>(Game::onInit, Event0::New),
            _onCycle = new Parasite<>(Game::onInit, Event0::New);
    private static final Parasite<Game, Event1<String>> _onException = new Parasite<>(Game::onInit, Event1::New);
    private static final Parasite<Game, Event1<AbstractPlayer>> _onBankrupt = new Parasite<>(Game::onInit, Event1::New);
    public static final EventWrapper<Game, Consumer0> onGameStart = new EventWrapper<>(_onGameStart),
            onGameOver = new EventWrapper<>(_onGameOver),
            onTurn = new EventWrapper<>(_onTurn),
            onLanded = new EventWrapper<>(_onLanded),
            onCycle = new EventWrapper<>(_onCycle);
    public static final EventWrapper<Game, Consumer1<String>> onException = new EventWrapper<>(_onException);
    public static final EventWrapper<Game, Consumer1<AbstractPlayer>> onBankrupt = new EventWrapper<>(_onBankrupt);

    void triggerBankrupt(AbstractPlayer player) {
        if (state != State.OVER) {
            players.remove(player);
            hadBankrupt = true;
            _onBankrupt.get(this).trigger( player);
        } else {
            logger.log(Level.WARNING, WRONG_STATE);
        }
    }

    public final void triggerException(String key, Object ...args) {
        _onException.get(this).trigger(format(key, args));
    }

    protected final Game readData(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        synchronized (lock) {
            return (Game)ois.readObject();
        }
    }

    protected final void writeData(ObjectOutputStream oos) throws IOException {
        synchronized (lock) {
            oos.writeObject(this);
        }
    }
}
