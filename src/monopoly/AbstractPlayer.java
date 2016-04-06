package monopoly;

import monopoly.card.Card;
import monopoly.stock.Stock;
import monopoly.util.*;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractPlayer implements Serializable, GameObject, Host {
    static {
        Game.putDefaultConfig("init-cash", 2000);
        Game.putDefaultConfig("init-deposit", 2000);
        Game.putDefaultConfig("init-coupons", 0);
        Game.putDefaultConfig("bank-max-transfer", 100000);
        Game.putDefaultConfig("stock-max-trade", 10000);
    }

    private static final Logger logger = Logger.getLogger(AbstractPlayer.class.getName());

    private static final Parasite<Game, Event3<AbstractPlayer, Integer, String>> _onMoneyChange = new Parasite<>(Game::onInit, Event3::New);
    private static final Parasite<Game, Event2<AbstractPlayer, Integer>> _onCouponChange = new Parasite<>(Game::onInit, Event2::New);
    private static final Parasite<Game, Event3<AbstractPlayer, Boolean, Card>> _onCardChange = new Parasite<>(Game::onInit, Event3::New);
    private static final Parasite<Game, Event3<AbstractPlayer, Stock, Integer>> _onStockHoldingChange = new Parasite<>(Game::onInit, Event3::New);

    public static final EventWrapper<Game, Consumer3<AbstractPlayer, Integer, String>> onMoneyChange = new EventWrapper<>(_onMoneyChange);
    public static final EventWrapper<Game, Consumer2<AbstractPlayer, Integer>> onCouponChange = new EventWrapper<>(_onCouponChange);
    public static final EventWrapper<Game, Consumer3<AbstractPlayer, Boolean, Card>> onCardChange = new EventWrapper<>(_onCardChange);
    public static final EventWrapper<Game, Consumer3<AbstractPlayer, Stock, Integer>> onStockHoldingChange = new EventWrapper<>(_onStockHoldingChange);

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

    private Game game;
    private String name;
    private Place currentPlace;
    private int cash, deposit, coupons;
    private boolean reversed = false;
    private final List<Card> cards = new CopyOnWriteArrayList<>();
    private final Shareholding shareholding = new Shareholding();
    private final java.util.Map<Object, Object> storage = new Hashtable<>();

    @Override
    @SuppressWarnings("unchecked")
    public final <T> T getParasite(Object key) {
        return (T) storage.get(key);
    }

    @Override
    public final void setParasite(Object key, Object value) {
        storage.put(key, value);
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
    
    protected void setGame(Game game) {
        synchronized (game.lock) {
            this.game = game;
            triggerGameInit(this);
            players.add(this);
        }
    }

    final void init() {
        if (game.getState() == Game.State.STARTING) {
            this.cash = game.getConfig("init-cash");
            this.deposit = game.getConfig("init-deposit");
            this.coupons = game.getConfig("init-coupons");
            currentPlace = game.getMap().getStartingPoint();
            cards.clear();
            reversed = false;
        } else {
            logger.log(Level.WARNING, Game.WRONG_STATE);
        }
    }

    public final Game getGame() {
        return game;
    }

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

    public final int getCash() {
        return cash;
    }

    public final int getDeposit() {
        return deposit;
    }

    public final int getCoupons() {
        return coupons;
    }

    public final Place getCurrentPlace() {
        return currentPlace;
    }

    public final List<Card> getCards() {
        return new CopyOnWriteArrayList<>(cards);
    }

    public final int getTotalPossessions() {
        return possessions.stream().map(f -> f.run(this)).reduce(0, (a, b) -> (a + b));
    }


    public final boolean isReversed() {
        return reversed;
    }

    final void reverse() {
        reversed = !reversed;
    }

    protected final void giveUp() {
        game.triggerBankrupt(this);
    }

    protected final void useCard(Card card, Consumer0 cb) {
        synchronized (game.lock) {
            if (game.getState() == Game.State.TURN_STARTING) {
                if (cards.contains(card)) {
                    cards.remove(card);
                    _onCardChange.get(game).trigger(this, false, card);
                    card.use(game, CardInterface.parasites.get(game), cb);
                } else {
                    game.triggerException("you_do_not_have_this_card");
                    cb.run();
                }
            } else {
                logger.log(Level.WARNING, Game.WRONG_STATE);
            }
        }
    }

    public final Shareholding.StockHolding getHolding(Stock stock) {
        return shareholding.getHolding(stock);
    }

    protected final void buyStock(Stock stock, int amount) {
        synchronized (game.lock) {
            if (game.getState() == Game.State.TURN_STARTING) {
                int maxTrade = game.getConfig("stock-max-trade");
                if (amount > maxTrade) {
                    game.triggerException("exceeded_max_stock_trade");
                } else if (amount < 0) {
                    game.triggerException("amount_cannot_be_negative");
                } else {
                    shareholding.buy(game, stock, amount);
                    _onStockHoldingChange.get(game).trigger(this, stock, amount);
                }
            } else {
                logger.log(Level.WARNING, Game.WRONG_STATE);
            }
        }
    }

    protected final void sellStock(Stock stock, int amount) {
        synchronized (game.lock) {
            if (game.getState() == Game.State.TURN_STARTING) {
                int maxTrade = game.getConfig("stock-max-trade");
                if (amount > maxTrade) {
                    game.triggerException("exceeded_max_stock_trade");
                } else if (amount < 0) {
                    game.triggerException("amount_cannot_be_negative");
                } else {
                    shareholding.sell(game, stock, amount);
                    _onStockHoldingChange.get(game).trigger(this, stock, -amount);
                }
            } else {
                logger.log(Level.WARNING, Game.WRONG_STATE);
            }
        }
    }

    protected abstract void startTurn(Consumer0 cb);
    protected abstract void askWhichCardToBuy(Consumer1<Card> cb);
    protected abstract void askHowMuchToDepositOrWithdraw(Consumer1<Integer> cb);
    public abstract void askForPlayer(String reason, Consumer1<AbstractPlayer> cb);
    public abstract void askForPlace(String reason, Consumer1<Place> cb);

    final void changeCash(int amount, String msg) {
        if (cash + amount >= 0) {
            cash += amount;
            _onMoneyChange.get(game).trigger(this, amount, msg);
        } else {
            game.triggerException("short_of_cash");
        }
    }

    final void changeDeposit(int amount, String msg) {
        if (deposit + amount >= 0) {
            deposit += amount;
            _onMoneyChange.get(game).trigger(this, amount, msg);
        } else {
            game.triggerException("short_of_deposit");
        }
    }

    final void depositOrWithdraw(Consumer0 cb) {
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
            propertySellers.get(i).run(this, () -> sellProperties(i+1, cb));
        } else {
            cb.run();
        }
    }

    final void pay(AbstractPlayer receiver, int amount, String msg, Consumer0 cb) {
        assert amount >= 0;
        cash -= amount;
        _onMoneyChange.get(game).trigger(this, -amount, msg);
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

    final void addCoupons(int amount) {
        coupons += amount;
        _onCouponChange.get(game).trigger(this, amount);
    }

    final void addCard(Card card) {
        cards.add(card);
        _onCardChange.get(game).trigger(this, true, card);
    }

    final void removeCard(Card card) {
        if (cards.contains(card)) {
            cards.remove(card);
            _onCardChange.get(game).trigger(this, false, card);
        }
    }

    final void buyCards(Consumer0 cb) {
        askWhichCardToBuy(new Consumer1<Card>() {
            @Override
            public void run(Card card) {
                synchronized (game.lock) {
                    if (card == null) {
                        cb.run();
                    } else {
                        int price = card.getPrice(game);
                        if (coupons >= price) {
                            cards.add(card);
                            coupons -= price;
                        }
                        askWhichCardToBuy(this);
                    }
                }
            }
        });
    }

    private int stepsToAdvance;

    final void startWalking(int steps) {
        if (game.getState() == Game.State.TURN_WALKING) {
            stepsToAdvance = steps;
            startStep();
        } else {
            logger.log(Level.WARNING, Game.WRONG_STATE);
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
                    currentPlace.onPassingBy(game, PlaceInterface.parasites.get(game), this::startStep);
                }
            } else {
                logger.log(Level.WARNING, Game.WRONG_STATE);
            }
        }
    }

    final void onLanded(Consumer0 cb) {
        currentPlace.onLanded(game, PlaceInterface.parasites.get(game), cb);
    }
}
