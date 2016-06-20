package monopoly;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import monopoly.place.Place;
import monopoly.util.*;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BasePlayer implements IPlayer {
    private static final Logger logger = Logger.getLogger(BasePlayer.class.getName());

    public static final Map<Game, InitEvent<IPlayer>> onAddPlayer = new Hashtable<>();
    public static final Map<Game, Event3<IPlayer, Double, String>> onMoneyChange = new Hashtable<>();
    public static final Map<Game, Event1<IPlayer>> onBankrupt = new Hashtable<>();
    public static final InitEvent<IPlayer> onInit = new InitEvent<>();

    static {
        Game.putDefaultConfig("init-cash", 2000.0);
        Game.putDefaultConfig("init-deposit", 2000.0);
        Game.putDefaultConfig("bank-max-transfer", 100000.0);
        Game.onInit.addListener(game -> {
            onAddPlayer.put(game, new InitEvent<>());
            onMoneyChange.put(game, new Event3<>());
            onBankrupt.put(game, new Event1<>());
        });
    }

    private final Game game;
    private final StringProperty name = new SimpleStringProperty();
    private final SimpleObjectProperty<Place> currentPlace = new SimpleObjectProperty<>();
    private final DoubleProperty cash = new SimpleDoubleProperty();
    private final DoubleProperty deposit = new SimpleDoubleProperty();
    private int stepsToAdvance;
    private boolean reversed = false, bankrupted = false;
    private final DoubleBinding totalPossessions;
    private final ObservableList<DoubleBinding> possessions = FXCollections.observableList(new CopyOnWriteArrayList<>());
    private final List<Consumer<Consumer0>> propertySellers = new CopyOnWriteArrayList<>();

    public BasePlayer(Game g) {
        synchronized (g.lock) {
            game = g;
            addPossession(Bindings.createDoubleBinding(cash::get, cash));
            addPossession(Bindings.createDoubleBinding(deposit::get, deposit));
            onInit.trigger(this);
            onAddPlayer.get(g).trigger(this);

            totalPossessions = Util.sum(possessions, e -> e);
        }
    }

    @Override
    public final Game getGame() {
        return game;
    }

    @Override
    public StringProperty nameProperty() {
        return name;
    }

    @Override
    public final String toString(Game game) {
        return getName();
    }

    @Override
    public final void addPossession(DoubleBinding possession) {
        possessions.add(possession);
    }

    @Override
    public final void addPropertySeller(Consumer<Consumer0> fn) {
        propertySellers.add(fn);
    }

    @Override
    public DoubleProperty cashProperty() {
        return cash;
    }

    @Override
    public DoubleProperty depositProperty() {
        return deposit;
    }

    @Override
    public SimpleObjectProperty<Place> currentPlaceProperty() {
        return currentPlace;
    }

    @Override
    public DoubleBinding totalPossessions() {
        return totalPossessions;
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
        onBankrupt.get(game).trigger(this);
        game.triggerBankrupt(this);
    }

    @Override
    public void giveUp() {
        synchronized (game.lock) {
            bankrupt();
        }
    }

    private void sellProperties(int i, Consumer0 cb) {
        if (i < propertySellers.size()) {
            propertySellers.get(i).accept(() -> sellProperties(i + 1, cb));
        } else {
            cb.accept();
        }
    }

    @Override
    public void pay(IPlayer receiver, double amount, String msg, Consumer0 cb) {
        synchronized (game.lock) {
            assert amount >= 0;
            cash.set(getCash() - amount);
            onMoneyChange.get(game).trigger(this, -amount, msg);
            if (receiver != null) {
                receiver.changeCash(Math.min(amount, getTotalPossessions() + amount), "");
            }
            if (getCash() < 0) {
                if (getCash() + getDeposit() >= 0) {
                    deposit.set(getDeposit() + getCash());
                    cash.set(0);
                    cb.accept();
                } else {
                    cash.set(getCash() + getDeposit());
                    deposit.set(0);
                    sellProperties(0, () -> {
                        if (getCash() <= 0) {
                            bankrupt();
                        }
                        cb.accept();
                    });
                }
            } else if (cb != null) {
                cb.accept();
            }
        }
    }

    @Override
    public void triggerOnMoneyChange(double amount, String msg) {
        onMoneyChange.get(game).trigger(this, amount, msg);
    }

    @Override
    public void triggerBankrupt() {
        onBankrupt.get(game).trigger(this);
    }

    protected void startStep() {
        endStep();
    }

    protected final void endStep() {
        synchronized (game.lock) {
            if (game.getState() == Game.State.TURN_WALKING) {
                currentPlace.set(reversed ? getCurrentPlace().getPrev() : getCurrentPlace().getNext());
                --stepsToAdvance;
                if (getCurrentPlace().hasRoadblock()) {
                    stepsToAdvance = 0;
                    getCurrentPlace().clearRoadblocks();
                    game.triggerException("met_roadblock", getCurrentPlace().toString(game));
                }
                if (stepsToAdvance <= 0) {
                    game.endWalking();
                } else {
                    getCurrentPlace().passBy(game, () -> {
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
            cash.set(game.getConfig("init-cash"));
            deposit.set(game.getConfig("init-deposit"));
            currentPlace.set(game.getMap().getStartingPoint());
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
