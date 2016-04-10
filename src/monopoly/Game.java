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

    public static final InitEvent<Game> onInit = new InitEvent<>();
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
        if (c == null) {
            c = defaultConfig;
        } else {
            c.setBase(defaultConfig);
        }
        config = new Config(c);

        updateMessages();
        onInit.trigger(this);
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
                onGameStart.trigger();
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
                dice = -1;
                if (players.isNewCycle() && notFirst) {
                    onCycle.trigger();
                }
                onTurn.trigger();
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

    private int dice = -1;

    public void setDice(int dice) {
        synchronized (lock) {
            if (state == State.TURN_STARTING) {
                if (dice >= 1 && dice < (Integer) config.get("dice-sides")) {
                    this.dice = dice;
                } else {
                    triggerException("invalid_dice_number");
                }
            } else {
                logger.log(Level.WARNING, WRONG_STATE);
                (new Exception()).printStackTrace();
            }
        }
    }

    public void startWalking() {
        synchronized (lock) {
            if (dice != -1) {
                startWalking(dice);
            } else {
                int dice = ThreadLocalRandom.current().nextInt(getConfig("dice-sides")) + 1;
                startWalking(dice);
            }
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
                onLanded.trigger();
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
            onGameOver.trigger();
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

    public final Event0 onGameStart = new Event0(),
            onGameOver = new Event0(),
            onTurn = new Event0(),
            onLanded = new Event0(),
            onCycle = new Event0();
    public final Event1<String> onException = new Event1<>();
    public final Event1<IPlayer> onBankrupt = new Event1<>();

    final void triggerBankrupt(IPlayer player) {
        if (state != State.OVER) {
            players.remove(player);
            hadBankrupt = true;
            onBankrupt.trigger( player);
        } else {
            logger.log(Level.WARNING, WRONG_STATE);
            (new Exception()).printStackTrace();
        }
    }

    public final void triggerException(String key, Object ...args) {
        onException.trigger(format(key, args));
    }

    protected static Game readData(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        synchronized (staticLock) {
            Game game = (Game) ois.readObject();
            onInit.trigger(game);
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
