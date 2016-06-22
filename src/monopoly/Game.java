package monopoly;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import monopoly.place.GameMap;
import monopoly.util.Event0;
import monopoly.util.Event1;
import monopoly.util.InitEvent;
import monopoly.util.Util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Game {
    private static final Logger logger = Logger.getLogger(Game.class.getName());
    static final String WRONG_STATE = "wrong state";

    public static final InitEvent<Game> onInit = new InitEvent<>();
    private static final Config defaultConfig = new Config();

    static {
        defaultConfig.put("bundle-name", "messages/messages");
        defaultConfig.put("locale", "zh-CN");
        defaultConfig.put("dice-sides", 6);
        defaultConfig.put("shuffle-players", false);
    }

    public enum State {
        OVER, STARTING, TURN_STARTING, TURN_WALKING, TURN_LANDED
    }

    public final Object lock = new Object();
    private State state = State.OVER;
    private final Config config;
    private transient ResourceBundle messages;
    private GameMap map;
    private final Players players = new Players();
    private final IntegerProperty dice = new SimpleIntegerProperty(1),
            daysPassed = new SimpleIntegerProperty(0);
    private boolean hadBankrupt = false;

    public static void putDefaultConfig(String key, Object value) {
        defaultConfig.put(key, value);
    }

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
        Locale locale = Locale.forLanguageTag(config.get("locale"));
        messages = ResourceBundle.getBundle(config.get("bundle-name"), locale);
    }

    public final void reset() {
        if (state == State.OVER) {
            players.reset();
            dice.set(1);
            daysPassed.set(0);
            hadBankrupt = false;
        }
    }

    public final State getState() {
        return state;
    }

    public final <T> T getConfig(String key) {
        synchronized (lock) {
            return config.get(key);
        }
    }

    final void putConfig(String key, Object value) {
        config.put(key, value);
        if (key.equals("bundle-name") || key.equals("locale")) {
            updateMessages();
        }
    }

    public final String getText(String key) {
        return Util.getText(messages, key);
    }

    public final String format(String key, Object... args) {
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

    public final void addPlayer(IPlayer player) {
        synchronized (lock) {
            if (state == State.OVER) {
                players.add(player);
            }
        }
    }

    public final void removePlayer(IPlayer player) {
        synchronized (lock) {
            if (state == State.OVER) {
                players.remove(player);
            }
        }
    }

    public final ObservableList<IPlayer> getPlayers() {
        return players.getPlayers();
    }

    public final ObjectBinding<IPlayer> currentPlayer() {
        return players.currentPlayer();
    }

    public final IPlayer getCurrentPlayer() {
        return players.currentPlayer().get();
    }

    public IntegerProperty diceProperty() {
        return dice;
    }

    public int getDice() {
        return dice.get();
    }

    public IntegerProperty daysPassedProperty() {
        return daysPassed;
    }

    public int getDaysPassed() {
        return daysPassed.get();
    }

    public final void start() {
        synchronized (lock) {
            if (state == State.OVER) {
                if (players.count() < 2) {
                    throw new RuntimeException("Too few players");
                }
                state = State.STARTING;
                if (config.get("shuffle-players")) {
                    players.shuffle();
                }
                map.init(this);
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
                nextDice = -1;
                if (players.isNewCycle() && notFirst) {
                    onCycle.trigger();
                }
                onTurn.trigger();
                getCurrentPlayer().startTurn(this::startWalking);
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
                        daysPassed.set(daysPassed.get() + 1);
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

    private int nextDice = -1;

    public void setDice(int dice) {
        synchronized (lock) {
            if (state == State.TURN_STARTING) {
                if (dice >= 1 && dice < (Integer) config.get("dice-sides")) {
                    nextDice = dice;
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
            if (nextDice != -1) {
                startWalking(nextDice);
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
                    onStartWalking.trigger(steps);
                    if (steps == 0) {
                        endWalking();
                    } else {
                        dice.set(steps);
                        getCurrentPlayer().startWalking(steps);
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
                getCurrentPlayer().getCurrentPlace().arriveAt(this, this::endTurn);
            }
        } else {
            logger.log(Level.WARNING, WRONG_STATE);
            (new Exception()).printStackTrace();
        }
    }

    public void endGame() {
        synchronized (lock) {
            if (state != State.OVER) {
                state = State.OVER;
                if (players.count() == 1) {
                    onGameOver.trigger(getCurrentPlayer());
                } else {
                    onGameOver.trigger(players.getPlayers().stream().reduce(
                            (a, b) -> a.getTotalPossessions() > b.getTotalPossessions()? a: b).orElse(null));
                }
                reset();
            } else {
                logger.log(Level.WARNING, WRONG_STATE);
                (new Exception()).printStackTrace();
            }
        }
    }

    public final Event0 onGameStart = new Event0(),
            onTurn = new Event0(),
            onLanded = new Event0(),
            onCycle = new Event0();
    public final Event1<Integer> onStartWalking = new Event1<>();
    public final Event1<String> onException = new Event1<>();
    public final Event1<IPlayer> onBankrupt = new Event1<>(),
            onGameOver = new Event1<>();

    final void triggerBankrupt(IPlayer player) {
        if (state != State.OVER) {
            players.remove(player);
            hadBankrupt = true;
            onBankrupt.trigger(player);
        } else {
            logger.log(Level.WARNING, WRONG_STATE);
            (new Exception()).printStackTrace();
        }
    }

    public final void triggerException(String key, Object... args) {
        onException.trigger(format(key, args));
    }
}
