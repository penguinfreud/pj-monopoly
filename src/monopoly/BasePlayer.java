package monopoly;

import monopoly.util.*;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BasePlayer implements IPlayer {
    private static final Logger logger = Logger.getLogger(BasePlayer.class.getName());

    public static final Parasite<Game, InitEvent<IPlayer>> onAddPlayer = new Parasite<>("BasePlayer.onAddPlayer");
    public static final Parasite<Game, Event3<IPlayer, Integer, String>> onMoneyChange = new Parasite<>("BasePlayer.onMoneyChange");
    public static final InitEvent<IPlayer> onInit = new InitEvent<>();

    static {
        Game.putDefaultConfig("init-cash", 2000);
        Game.putDefaultConfig("init-deposit", 2000);
        Game.putDefaultConfig("bank-max-transfer", 100000);
        Game.onInit.addListener(game -> {
            onAddPlayer.set(game, new InitEvent<>());
            onMoneyChange.set(game, new Event3<>());
        });
    }

    private Game game;
    private String name;
    private Place currentPlace;
    private int cash, deposit;
    private int stepsToAdvance;
    private boolean reversed = false, bankrupted = false;
    private final List<Supplier<Integer>> possessions = new CopyOnWriteArrayList<>();
    private final List<Consumer1<Consumer0>> propertySellers = new CopyOnWriteArrayList<>();
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

    public BasePlayer(Game g) {
        this("", g);
    }

    public BasePlayer(String name, Game g) {
        synchronized (g.lock) {
            game = g;
            this.name = name;
            addPossession(this::getCash);
            addPossession(this::getDeposit);
            onInit.trigger(this);
            onAddPlayer.get(g).trigger(this);
        }
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
    public final void addPossession(Supplier<Integer> possession) {
        possessions.add(possession);
    }

    @Override
    public final void addPropertySeller(Consumer1<Consumer0> fn) {
        propertySellers.add(fn);
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
        synchronized (game.lock) {
            return possessions.stream().map(Supplier::run).reduce(0, (a, b) -> (a + b));
        }
    }

    @Override
    public final boolean isReversed() {
        return reversed;
    }

    @Override
    public void reverse() {
        reversed = !reversed;
    }

    private void bankrupt() {
        bankrupted = true;
        game.triggerBankrupt(this);
    }

    @Override
    public void giveUp() {
        bankrupt();
    }

    @Override
    public void changeCash(int amount, String msg) {
        synchronized (game.lock) {
            if (cash + amount >= 0 || cash < 0 && amount >= 0) {
                cash += amount;
                onMoneyChange.get(game).trigger(this, amount, msg);
            } else {
                game.triggerException("short_of_cash");
            }
        }
    }

    @Override
    public void changeDeposit(int amount, String msg) {
        synchronized (game.lock) {
            if (deposit + amount >= 0) {
                deposit += amount;
                onMoneyChange.get(game).trigger(this, amount, msg);
            } else {
                game.triggerException("short_of_deposit");
            }
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
            propertySellers.get(i).run(() -> sellProperties(i+1, cb));
        } else {
            cb.run();
        }
    }

    @Override
    public void pay(IPlayer receiver, int amount, String msg, Consumer0 cb) {
        synchronized (game.lock) {
            assert amount >= 0;
            cash -= amount;
            onMoneyChange.get(game).trigger(this, -amount, msg);
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
                    sellProperties(0, () -> {
                        if (cash <= 0) {
                            bankrupt();
                        }
                        cb.run();
                    });
                }
            } else if (cb != null) {
                cb.run();
            }
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
                if (stepsToAdvance <= 0) {
                    game.endWalking();
                } else {
                    currentPlace.passBy(game, () -> {
                        if (bankrupted) {
                            game.endWalking();
                        } else {
                            startStep();
                        }
                    });
                }
            } else {
                logger.log(Level.WARNING, Game.WRONG_STATE);
                (new Exception()).printStackTrace();
            }
        }
    }

    @Override
    public final void init() {
        if (game.getState() == Game.State.STARTING) {
            cash = game.getConfig("init-cash");
            deposit = game.getConfig("init-deposit");
            currentPlace = game.getMap().getStartingPoint();
            reversed = false;
            bankrupted = false;
        } else {
            logger.log(Level.WARNING, Game.WRONG_STATE);
            (new Exception()).printStackTrace();
        }
    }

    @Override
    public final void startWalking(int steps) {
        if (game.getState() == Game.State.TURN_WALKING) {
            stepsToAdvance = steps;
            startStep();
        } else {
            logger.log(Level.WARNING, Game.WRONG_STATE);
            (new Exception()).printStackTrace();
        }
    }
}
