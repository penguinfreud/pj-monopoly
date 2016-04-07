package monopoly;

import monopoly.util.*;

import java.io.*;
import java.lang.ref.WeakReference;
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
    private static final List<WeakReference<Game>> games = new CopyOnWriteArrayList<>();
    private static final Config defaultConfig = new Config();

    static {
        defaultConfig.put("bundle-name", "messages");
        defaultConfig.put("locale", "zh-CN");
        defaultConfig.put("dice-sides", 6);
        defaultConfig.put("shuffle-players", true);
    }

    enum State {
        OVER, STARTING, TURN_STARTING, TURN_WALKING, TURN_LANDED
    }
    
    private State state = State.OVER;
    private final Config config;
    private transient ResourceBundle messages;
    private GameMap map;
    private final Players players = new Players();
    private boolean hadBankrupt = false;
    private final Map<Object, Object> storage = new Hashtable<>();

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
            games.add(new WeakReference<>(this));
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

    public final <T> Consumer1<T> sync(Consumer1<T> cb) {
        return t -> {
            synchronized (lock) {
                cb.run(t);
            }
        };
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

    final void putConfig(String key, Object value) {
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
            //logger.log(Level.INFO, "Unknown key: " + key);
            return key;
        }
    }

    public final String format(String key, Object ...args) {
        return MessageFormat.format(getText(key), args);
    }

    public final GameMap getMap() {
        return map;
    }

    public final void setMap(GameMap map) {
        synchronized (lock) {
            if (state == State.OVER) {
                this.map = map;
            } else {
                logger.log(Level.WARNING, WRONG_STATE);
                (new Exception()).printStackTrace();
            }
        }
    }

    public final void setPlayers(List<IPlayer> playersList) throws Exception {
        synchronized (lock) {
            if (state == State.OVER) {
                playersList.stream().forEach((player) -> player.setGame(this));
                players.set(playersList);
                if ((boolean) config.get("shuffle-players")) {
                    players.shuffle();
                }
            } else {
                logger.log(Level.WARNING, WRONG_STATE);
                (new Exception()).printStackTrace();
            }
        }
    }

    public final List<IPlayer> getPlayers() {
        return players.getPlayers();
    }

    public final IPlayer getCurrentPlayer() {
        return players.getCurrentPlayer();
    }

    public final void start() {
        synchronized (lock) {
            if (state == State.OVER) {
                state = State.STARTING;
                players.init();
                _onGameStart.get(this).trigger();
                startTurn();
            } else {
                logger.log(Level.WARNING, WRONG_STATE);
                (new Exception()).printStackTrace();
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
                (new Exception()).printStackTrace();
            }
        }
    }

    private boolean inEndTurn = false;
    private boolean tailRecursion = false;

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
                (new Exception()).printStackTrace();
            }
        }
    }

    public void startWalking() {
        synchronized (lock) {
            int dice = ThreadLocalRandom.current().nextInt(getConfig("dice-sides")) + 1;
            startWalking(dice);
        }
    }

    public final void startWalking(int steps) {
        synchronized (lock) {
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
                (new Exception()).printStackTrace();
            }
        }
    }

    final void endWalking() {
        if (state == State.TURN_WALKING) {
            state = State.TURN_LANDED;
            if (hadBankrupt) {
                endTurn();
            } else {
                _onLanded.get(this).trigger();
                players.getCurrentPlayer().getCurrentPlace().arriveAt(this, this::endTurn);
            }
        } else {
            logger.log(Level.WARNING, WRONG_STATE);
            (new Exception()).printStackTrace();
        }
    }

    private void endGame() {
        if (state != State.OVER) {
            state = State.OVER;
            _onGameOver.get(this).trigger();
        } else {
            logger.log(Level.WARNING, WRONG_STATE);
            (new Exception()).printStackTrace();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <T> T getParasite(Object key) {
        return (T) storage.get(key);
    }

    @Override
    public final void setParasite(Object key, Object value) {
        storage.put(key, value);
    }

    public static void onInit(Consumer1<Game> listener) {
        synchronized (staticLock) {
            _onGameInit.add(listener);
            for (int i = games.size() - 1; i>=0; i--) {
                Game g = games.get(i).get();
                if (g == null) {
                    games.remove(i);
                } else {
                    listener.run(g);
                }
            }
        }
    }

    private static void triggerGameInit(Game g) {
        synchronized (staticLock) {
            for (Consumer1<Game> listener : _onGameInit) {
                listener.run(g);
            }
        }
    }

    private static final Parasite<Game, Event0> _onGameStart = new Parasite<>("Game.onGameStart", Game::onInit, Event0::New),
            _onGameOver = new Parasite<>("Game.onGameOver", Game::onInit, Event0::New),
            _onTurn = new Parasite<>("Game.onTurn", Game::onInit, Event0::New),
            _onLanded = new Parasite<>("Game.onLanded", Game::onInit, Event0::New),
            _onCycle = new Parasite<>("Game.onCycle", Game::onInit, Event0::New);
    private static final Parasite<Game, Event1<String>> _onException = new Parasite<>("Game.onException", Game::onInit, Event1::New);
    private static final Parasite<Game, Event1<IPlayer>> _onBankrupt = new Parasite<>("Game.onBankrupt", Game::onInit, Event1::New);
    public static final EventWrapper<Game, Consumer0> onGameStart = new EventWrapper<>(_onGameStart),
            onGameOver = new EventWrapper<>(_onGameOver),
            onTurn = new EventWrapper<>(_onTurn),
            onLanded = new EventWrapper<>(_onLanded),
            onCycle = new EventWrapper<>(_onCycle);
    public static final EventWrapper<Game, Consumer1<String>> onException = new EventWrapper<>(_onException);
    public static final EventWrapper<Game, Consumer1<IPlayer>> onBankrupt = new EventWrapper<>(_onBankrupt);

    final void triggerBankrupt(IPlayer player) {
        if (state != State.OVER) {
            players.remove(player);
            hadBankrupt = true;
            _onBankrupt.get(this).trigger( player);
        } else {
            logger.log(Level.WARNING, WRONG_STATE);
            (new Exception()).printStackTrace();
        }
    }

    public final void triggerException(String key, Object ...args) {
        _onException.get(this).trigger(format(key, args));
    }

    protected static Game readData(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        synchronized (staticLock) {
            Game game = (Game) ois.readObject();
            triggerGameInit(game);
            games.add(new WeakReference<>(game));
            return game;
        }
    }

    protected final void writeData(ObjectOutputStream oos) throws IOException {
        synchronized (lock) {
            if (state != State.OVER) {
                oos.writeObject(this);
            }
        }
    }
}
