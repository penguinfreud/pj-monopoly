package monopoly;

import monopoly.util.*;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractPlayer implements IPlayer {
    static {
        Game.putDefaultConfig("init-cash", 2000);
        Game.putDefaultConfig("init-deposit", 2000);
        Game.putDefaultConfig("bank-max-transfer", 100000);
    }

    private static final Logger logger = Logger.getLogger(AbstractPlayer.class.getName());

    private static final Parasite<Game, Event3<AbstractPlayer, Integer, String>> _onMoneyChange = new Parasite<>(Game::onInit, Event3::New);

    public static final EventWrapper<Game, Consumer3<AbstractPlayer, Integer, String>> onMoneyChange = new EventWrapper<>(_onMoneyChange);

    private static final SerializableObject staticLock = new SerializableObject();
    private static final List<Consumer1<AbstractPlayer>> _onInit = new CopyOnWriteArrayList<>();
    private static final List<AbstractPlayer> players = new CopyOnWriteArrayList<>();

    private static final List<Function1<AbstractPlayer, Integer>> possessions = new CopyOnWriteArrayList<>();
    private static final List<Consumer2<AbstractPlayer, Consumer0>> propertySellers = new CopyOnWriteArrayList<>();

    static void addPossession(Function1<AbstractPlayer, Integer> possession) {
        possessions.add(possession);
    }

    static void addPropertySeller(Consumer2<AbstractPlayer, Consumer0> fn) {
        propertySellers.add(fn);
    }

    static {
        possessions.add(AbstractPlayer::getCash);
        possessions.add(AbstractPlayer::getDeposit);
    }

    public static void onInit(Consumer1<AbstractPlayer> listener) {
        synchronized (staticLock) {
            _onInit.add(listener);
            players.stream().forEach(listener::run);
        }
    }

    private static void triggerGameInit(AbstractPlayer player) {
        synchronized (staticLock) {
            for (Consumer1<AbstractPlayer> listener: _onInit) {
                listener.run(player);
            }
        }
    }

    private Game game;
    private String name;
    private Place currentPlace;
    private int cash, deposit;
    private int stepsToAdvance;
    private boolean reversed = false;
    private final Map<Object, Object> storage = new Hashtable<>();

    @Override
    @SuppressWarnings("unchecked")
    public final <T> T getParasite(Object key) {
        return (T) storage.get(key);
    }

    @Override
    public final void setParasite(Object key, Object value) {
        storage.put(key, value);
    }

    @Override
    public final Game getGame() {
        return game;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final String toString(Game game) {
        return name;
    }

    protected final void setName(String name) {
        this.name = name;
    }

    @Override
    public final int getCash() {
        return cash;
    }

    @Override
    public final int getDeposit() {
        return deposit;
    }

    @Override
    public final Place getCurrentPlace() {
        return currentPlace;
    }

    @Override
    public final int getTotalPossessions() {
        return possessions.stream().map(f -> f.run(this)).reduce(0, (a, b) -> (a + b));
    }

    @Override
    public final boolean isReversed() {
        return reversed;
    }

    protected abstract void startTurn(Consumer0 cb);
    public abstract void askHowMuchToDepositOrWithdraw(Consumer1<Integer> cb);

    @Override
    public void reverse() {
        reversed = !reversed;
    }

    @Override
    public void giveUp() {
        game.triggerBankrupt(AbstractPlayer.this);
    }

    @Override
    public void changeCash(int amount, String msg) {
        if (cash + amount >= 0) {
            cash += amount;
            _onMoneyChange.get(game).trigger(AbstractPlayer.this, amount, msg);
        } else {
            game.triggerException("short_of_cash");
        }
    }

    @Override
    public void changeDeposit(int amount, String msg) {
        if (deposit + amount >= 0) {
            deposit += amount;
            _onMoneyChange.get(game).trigger(AbstractPlayer.this, amount, msg);
        } else {
            game.triggerException("short_of_deposit");
        }
    }

    @Override
    public void depositOrWithdraw(Consumer0 cb) {
        askHowMuchToDepositOrWithdraw((amount) -> {
            int maxTransfer = game.getConfig("bank-max-transfer");
            if (-maxTransfer <= amount && amount <= maxTransfer) {
                if (cash - amount >= 0 && deposit + amount >= 0) {
                    cash -= amount;
                    deposit += amount;
                }
            } else {
                game.triggerException("exceeded_max_transfer_credits");
            }
            cb.run();
        });
    }

    private void sellProperties(int i, Consumer0 cb) {
        if (i < propertySellers.size()) {
            propertySellers.get(i).run(AbstractPlayer.this, () -> sellProperties(i+1, cb));
        } else {
            cb.run();
        }
    }

    @Override
    public void pay(AbstractPlayer receiver, int amount, String msg, Consumer0 cb) {
        assert amount >= 0;
        cash -= amount;
        _onMoneyChange.get(game).trigger(AbstractPlayer.this, -amount, msg);
        if (receiver != null) {
            receiver.changeCash(Math.min(amount, getTotalPossessions() + amount), "");
        }
        if (cash < 0) {
            if (cash + deposit >= 0) {
                deposit += cash;
                cash = 0;
                cb.run();
            } else {
                cash += deposit;
                deposit = 0;
                sellProperties(0, cb);
            }
        } else if (cb != null) {
            cb.run();
        }
    }

    protected void startStep() {
        endStep();
    }

    protected final void endStep() {
        synchronized (game.lock) {
            if (game.getState() == Game.State.TURN_WALKING) {
                currentPlace = reversed? currentPlace.getPrev(): currentPlace.getNext();
                --stepsToAdvance;
                if (currentPlace.hasRoadblock()) {
                    stepsToAdvance = 0;
                    currentPlace.clearRoadblocks();
                    game.triggerException("met_roadblock", currentPlace.toString(game));
                }
                if (stepsToAdvance == 0) {
                    game.endWalking();
                } else {
                    currentPlace.passBy(game, this::startStep);
                }
            } else {
                logger.log(Level.WARNING, Game.WRONG_STATE);
            }
        }
    }

    final void init() {
        if (game.getState() == Game.State.STARTING) {
            cash = game.getConfig("init-cash");
            deposit = game.getConfig("init-deposit");
            currentPlace = game.getMap().getStartingPoint();
            reversed = false;
        } else {
            logger.log(Level.WARNING, Game.WRONG_STATE);
        }
    }

    protected void setGame(Game g) {
        synchronized (g.lock) {
            game = g;
            triggerGameInit(AbstractPlayer.this);
            players.add(AbstractPlayer.this);
        }
    }

    final void startWalking(int steps) {
        if (game.getState() == Game.State.TURN_WALKING) {
            stepsToAdvance = steps;
            startStep();
        } else {
            logger.log(Level.WARNING, Game.WRONG_STATE);
        }
    }

    protected void useCard(Card card, Consumer0 cb) {
        synchronized (game.lock) {
            card.use(game, cb);
        }
    }
}
