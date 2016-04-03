package monopoly;

import monopoly.util.*;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractPlayer implements Serializable, GameObject {
    static {
        Game.putDefaultConfig("init-cash", 2000);
        Game.putDefaultConfig("init-deposit", 2000);
        Game.putDefaultConfig("init-coupons", 0);
    }

    private static final Event<Triple<AbstractPlayer, Integer, String>> _onMoneyChange = new Event<>();
    private static final Event<Pair<AbstractPlayer, Integer>> _onGetCoupons = new Event<>();
    private static final Event<Pair<AbstractPlayer, Card>> _onGetCard = new Event<>();
    public static final EventWrapper<Triple<AbstractPlayer, Integer, String>> onMoneyChange = new EventWrapper<>(_onMoneyChange);
    public static final EventWrapper<Pair<AbstractPlayer, Integer>> onGetCoupons = new EventWrapper<>(_onGetCoupons);
    public static final EventWrapper<Pair<AbstractPlayer, Card>> onGetCard = new EventWrapper<>(_onGetCard);

    private String name;
    private Place currentPlace;
    private int cash, deposit, coupons;
    private boolean reversed = false;
    private final List<Property> properties = new CopyOnWriteArrayList<>();
    private final List<Card> cards = new CopyOnWriteArrayList<>();

    final void init(Game g) {
        this.cash = (Integer) g.getConfig("init-cash");
        this.deposit = (Integer) g.getConfig("init-deposit");
        this.coupons = (Integer) g.getConfig("init-coupons");
        currentPlace = g.getMap().getStartingPoint();
        properties.clear();
        cards.clear();
        reversed = false;
    }

    public final String getName() {
        return name;
    }

    @Override
    public String toString(Game g) {
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

    public final List<Property> getProperties() {
        return new CopyOnWriteArrayList<>(properties);
    }

    public final List<Card> getCards() {
        return new CopyOnWriteArrayList<>(cards);
    }

    public final int getTotalPossessions() {
        int poss = cash + deposit;
        for (Property prop: properties) {
            poss += prop.getMortgagePrice();
        }
        return poss;
    }

    public final boolean isReversed() {
        return reversed;
    }

    protected final void giveUp(Game g) {
        g.triggerBankrupt(this);
    }

    private final Callback<Object> useCardCb = new Callback<Object>() {
        @Override
        public void run(Game g, Object o) {
            synchronized (g.lock) {
                if (g.getState() == Game.State.TURN_STARTING) {
                    askWhichCardToUse(g, (_g, card) -> {
                        synchronized (g.lock) {
                            if (_g.getState() == Game.State.TURN_STARTING) {
                                if (card == null || !cards.contains(card)) {
                                    _g.rollTheDice();
                                } else {
                                    cards.remove(card);
                                    _g.useCard(card, this);
                                }
                            }
                        }
                    });
                }
            }
        }
    };

    final void startTurn(Game g) {
        useCardCb.run(g, null);
    }

    protected abstract void askWhetherToBuyProperty(Game g, Callback<Boolean> cb);
    protected abstract void askWhetherToUpgradeProperty(Game g, Callback<Boolean> cb);
    protected abstract void askWhichPropertyToMortgage(Game g, Callback<Property> cb);
    protected abstract void askWhichCardToBuy(Game g, Callback<Card> cb);
    protected abstract void askWhichCardToUse(Game g, Callback<Card> cb);
    protected abstract void askHowMuchToDepositOrWithdraw(Game g, Callback<Integer> cb);

    public abstract void askForPlayer(Game g, String reason, Callback<AbstractPlayer> cb);
    public abstract void askForPlace(Game g, String reason, Callback<Place> cb);

    private void changeCash(Game g, int amount, String msg) {
        if (cash + amount >= 0) {
            cash += amount;
            _onMoneyChange.trigger(g, new Triple<>(this, amount, msg));
        }
    }

    final void changeDeposit(Game g, int amount, String msg) {
        if (deposit + amount >= 0) {
            deposit += amount;
            _onMoneyChange.trigger(g, new Triple<>(this, amount, msg));
        } else {
            g.triggerException("deposit_not_enough");
        }
    }

    private void sellProperties(Game g, Property prop, Callback<Object> cb) {
        if (cash <= 0) {
            if (prop != null) {
                if (properties.contains(prop)) {
                    cash += prop.getMortgagePrice();
                    properties.remove(prop);
                    prop.resetOwner();
                } else {
                    g.triggerException("not_your_property");
                }
            }
            if (cash <= 0) {
                if (properties.size() > 0) {
                    askWhichPropertyToMortgage(g, (_g, nextProp) -> sellProperties(_g, nextProp, cb));
                } else {
                    g.triggerBankrupt(this);
                    cb.run(g, null);
                }
            } else {
                cb.run(g, null);
            }
        } else {
            cb.run(g, null);
        }
    }

    private boolean checkBuyingCondition(Game g, boolean force) {
        Property prop = currentPlace.asProperty();
        if (cash >= prop.getPurchasePrice()) {
            if (prop.isFree()) {
                return true;
            } else if (force) {
                AbstractPlayer owner = prop.getOwner();
                if (owner != this) {
                    return true;
                } else {
                    g.triggerException("you_cannot_buy_your_own_land");
                }
            } else {
                g.triggerException("cannot_buy_sold_land");
            }
        } else {
            g.triggerException("not_enough_cash");
        }
        return false;
    }

    private void _buyProperty(Game g, boolean force) {
        if (g.getState() == Game.State.TURN_LANDED) {
            Property prop = currentPlace.asProperty();
            int price = prop.getPurchasePrice();
            if (checkBuyingCondition(g, force)) {
                AbstractPlayer owner = prop.getOwner();
                pay(g, owner, price, "buy_property", null);
                properties.add(prop);
                if (owner != null) {
                    owner.properties.remove(prop);
                }
                prop.changeOwner(this);
            }
        }
    }

    private void _upgradeProperty(Game g) {
        if (g.getState() == Game.State.TURN_LANDED) {
            Property prop = currentPlace.asProperty();
            int price = prop.getUpgradePrice();
            if (prop.getOwner() == this) {
                if (cash >= price) {
                    changeCash(g, -price, "upgrade_property");
                    prop.upgrade(g);
                } else {
                    g.triggerException("not_enough_cash");
                }
            } else {
                g.triggerException("not_your_property");
            }
        }
    }

    final void payRent(Game g, Callback<Object> cb) {
        if (g.getState() == Game.State.TURN_LANDED) {
            Property prop = currentPlace.asProperty();
            pay(g, prop.getOwner(), prop.getRent(), "pay_rent", cb);
        }
    }

    private void buyProperty(Game g, Callback<Object> cb, boolean force) {
        synchronized (g.lock) {
            if (g.getState() == Game.State.TURN_LANDED) {
                Property prop = currentPlace.asProperty();
                int price = prop.getPurchasePrice();
                if (prop.isFree() && cash >= price) {
                    askWhetherToBuyProperty(g, (_g, ok) -> {
                        synchronized (g.lock) {
                            if (ok) {
                                _buyProperty(g, force);
                            }
                            cb.run(g, null);
                        }
                    });
                } else {
                    cb.run(g, null);
                }
            }
        }
    }

    final void buyProperty(Game g, Callback<Object> cb) {
        buyProperty(g, cb, false);
    }

    final void upgradeProperty(Game g, Callback<Object> cb) {
        synchronized (g.lock) {
            if (g.getState() == Game.State.TURN_LANDED) {
                Property prop = currentPlace.asProperty();
                int price = prop.getUpgradePrice();
                if (prop.getOwner() == this && cash >= price) {
                    askWhetherToUpgradeProperty(g, (_g, ok) -> {
                        synchronized (_g.lock) {
                            if (ok) {
                                _upgradeProperty(_g);
                            }
                            cb.run(_g, null);
                        }
                    });
                } else {
                    cb.run(g, null);
                }
            }
        }
    }

    final void pay(Game g, AbstractPlayer receiver, int amount, String msg, Callback<Object> cb) {
        cash -= amount;
        _onMoneyChange.trigger(g, new Triple<>(this, -amount, msg));
        if (receiver != null) {
            receiver.changeCash(g, Math.min(amount, getTotalPossessions() + amount), "get_" + msg);
        }
        if (cash <= 0) {
            if (cash + deposit >= 0) {
                deposit += cash;
                cash = 0;
                cb.run(g, null);
            } else {
                cash += deposit;
                deposit = 0;
                sellProperties(g, null, cb);
            }
        } else if (cb != null) {
            cb.run(g, null);
        }
    }

    private int stepsToAdvance;

    final void startWalking(Game g, int steps) {
        if (g.getState() == Game.State.TURN_WALKING) {
            stepsToAdvance = steps;
            startStep(g);
        }
    }

    protected void startStep(Game g) {
        endStep(g);
    }

    protected final void endStep(Game g) {
        synchronized (g.lock) {
            if (g.getState() == Game.State.TURN_WALKING) {
                currentPlace = reversed? currentPlace.getPrev(): currentPlace.getNext();
                --stepsToAdvance;
                if (currentPlace.hasRoadblock()) {
                    stepsToAdvance = 0;
                    currentPlace.clearRoadblocks();
                    g.triggerException("met_roadblock", currentPlace.getName());
                }
                if (stepsToAdvance == 0) {
                    g.endWalking();
                } else {
                    g.passBy(currentPlace, (_g, o) -> startStep(_g));
                }
            }
        }
    }

    public static final class PlaceInterface implements Serializable {
        public final void changeCash(AbstractPlayer player, Game g, int amount, String msg) {
            synchronized (g.lock) {
                player.changeCash(g, amount, msg);
            }
        }

        public final void changeDeposit(AbstractPlayer player, Game g, int amount, String msg) {
            synchronized (g.lock) {
                player.changeDeposit(g, amount, msg);
            }
        }

        public final void depositOrWithdraw(Game g, Callback<Object> cb) {
            synchronized (g.lock) {
                AbstractPlayer player = g.getCurrentPlayer();
                player.askHowMuchToDepositOrWithdraw(g, (_g, amount) -> {
                    int maxTransfer = (Integer) _g.getConfig("bank-max-transfer");
                    if (-maxTransfer <= amount && amount <= maxTransfer &&
                            player.cash - amount >= 0 && player.deposit + amount >= 0) {
                        player.cash -= amount;
                        player.deposit += amount;
                    }
                    cb.run(_g, null);
                });
            }
        }

        public final void pay(AbstractPlayer player, Game g, AbstractPlayer receiver, int amount, String msg, Callback<Object> cb) {
            synchronized (g.lock) {
                player.pay(g, receiver, amount, msg, cb);
            }
        }

        public final void addCoupons(AbstractPlayer player, Game g, int amount) {
            synchronized (g.lock) {
                player.coupons += amount;
                _onGetCoupons.trigger(g, new Pair<>(player, amount));
            }
        }

        public final void addCard(AbstractPlayer player, Game g, Card card) {
            synchronized (g.lock) {
                player.cards.add(card);
                _onGetCard.trigger(g, new Pair<>(player, card));
            }
        }

        public final void buyCards(Game g, Callback<Object> cb) {
            synchronized (g.lock) {
                AbstractPlayer player = g.getCurrentPlayer();
                player.askWhichCardToBuy(g, new Callback<Card>() {
                    @Override
                    public void run(Game g, Card card) {
                        synchronized (g.lock) {
                            if (card == null) {
                                cb.run(g, null);
                            } else {
                                int price = card.getPrice(g);
                                if (player.coupons >= price) {
                                    player.cards.add(card);
                                    player.coupons -= price;
                                }
                                player.askWhichCardToBuy(g, this);
                            }
                        }
                    }
                });
            }
        }
    }

    public static final class CardInterface implements Serializable {
        public final SerializableObject lock;

        public final void reverse(AbstractPlayer player) {
            player.reversed = !player.reversed;
        }

        public CardInterface(Game g) {
            lock = g.lock;
        }

        public final void walk(Game g, int steps) {
            synchronized (g.lock) {
                if (steps > 0 && steps <= (Integer) g.getConfig("dice-sides")) {
                    g.startWalking(steps);
                }
            }
        }

        public final void stay(Game g) {
            synchronized (g.lock) {
                g.stay();
            }
        }

        public final void setRoadblock(Game g, Place place) {
            synchronized (g.lock) {
                place.setRoadblock();
            }
        }

        public final void buyProperty(Game g, Callback<Object> cb) {
            synchronized (g.lock) {
                g.getCurrentPlayer().buyProperty(g, cb, true);
            }
        }

        public final void changeCash(AbstractPlayer player, Game g, int amount, String msg) {
            synchronized (g.lock) {
                player.changeCash(g, amount, msg);
            }
        }

        public final void changeDeposit(AbstractPlayer player, Game g, int amount, String msg) {
            synchronized (g.lock) {
                player.changeDeposit(g, amount, msg);
            }
        }

        public final void robLand(Game g) {
            synchronized (g.lock) {
                AbstractPlayer player = g.getCurrentPlayer();
                Place place = player.getCurrentPlace();
                Property prop = place.asProperty();
                if (prop != null) {
                    AbstractPlayer owner = prop.getOwner();
                    if (owner != null) {
                        owner.properties.remove(prop);
                    }
                    prop.changeOwner(player);
                }
            }
        }

        public final void resetLevel(Game g, Property prop) {
            synchronized (g.lock) {
                prop.resetLevel();
            }
        }

        public final void resetOwner(Game g, Property prop) {
            synchronized (g.lock) {
                prop.resetOwner();
            }
        }
    }
}
