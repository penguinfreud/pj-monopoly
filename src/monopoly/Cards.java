package monopoly;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import monopoly.card.LotteryCard;
import monopoly.card.ReverseCard;
import monopoly.card.Roadblock;
import monopoly.place.Place;
import monopoly.util.Consumer0;
import monopoly.util.Event2;
import monopoly.util.Event3;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Cards {
    public interface IPlayerWithCards extends IPlayer {
        default void askWhichCardToBuy(Consumer<Card> cb) {
            int coupons = Cards.get(this).getCoupons();
            if (coupons == 0) {
                cb.accept(null);
            } else {
                Object[] buyableCards = availableCards.get(getGame()).stream()
                        .filter((card) -> card.getPrice(getGame()) <= coupons).toArray();
                if (buyableCards.length == 0) {
                    cb.accept(null);
                } else {
                    cb.accept((Card) buyableCards[ThreadLocalRandom.current().nextInt(buyableCards.length)]);
                }
            }
        }

        default void askForTargetPlayer(Card card, Consumer<IPlayer> cb) {
            if (card instanceof ReverseCard) {
                cb.accept(this);
            } else {
                List<IPlayer> players = getGame().getPlayers();
                IPlayer first = players.get(0);
                cb.accept(first == this ? players.get(1) : first);
            }
        }

        default void askForTargetPlace(Card card, List<Place> candidates, Consumer<Place> cb) {
            cb.accept(candidates.get(0));
        }

        default void askForInt(Card card, Consumer<Integer> cb) {
            if (card instanceof LotteryCard) {
                cb.accept(0);
            } else {
                cb.accept(0);
            }
        }

        @Override
        default void startTurn(Consumer0 cb) {
            Cards cardsInterface = Cards.get(this);
            List<Card> cards = cardsInterface.cards;
            if (cards.isEmpty()) {
                cb.accept();
            } else {
                int l = cards.size();
                cardsInterface.useCard(cards.get(l - 1), new Consumer0() {
                    private int count = l,
                            i = l - 1;
                    private Card card = cards.get(0);

                    @Override
                    public void accept() {
                        if (cards.size() == count && cards.get(count - 1) == card) {
                            i--;
                        } else {
                            count = cards.size();
                        }
                        if (i < 0 || i >= count) {
                            cb.accept();
                        } else {
                            card = cards.get(i);
                            cardsInterface.useCard(card, this);
                        }
                    }
                });
            }
        }
    }

    static {
        Game.putDefaultConfig("enable-coupons", 0);
    }

    private static final Map<IPlayer, Cards> parasites = new Hashtable<>();
    private static final Map<Game, List<Card>> availableCards = new Hashtable<>();
    public static final Map<Game, Event2<IPlayer, Integer>> onCouponChange = new Hashtable<>();
    public static final Map<Game, Event3<IPlayer, Boolean, Card>> onCardChange = new Hashtable<>();

    public static Cards get(IPlayer player) {
        return parasites.get(player);
    }

    public static void enable(Game g) {
        if (availableCards.get(g) == null) {
            availableCards.put(g, new CopyOnWriteArrayList<>());
            onCouponChange.put(g, new Event2<>());
            onCardChange.put(g, new Event3<>());
            BasePlayer.onAddPlayer.get(g).addListener(player ->
                    parasites.put(player, new Cards(player)));
        }
    }

    public static void enableCard(Game g, Card card) {
        List<Card> cards = availableCards.get(g);
        if (cards == null) {
            enable(g);
            cards = availableCards.get(g);
        }
        if (!cards.contains(card)) {
            cards.add(card);
        }
    }

    public static boolean isEnabled(Game g) {
        return availableCards.get(g) != null;
    }

    public static boolean isCardEnabled(Game g, Card card) {
        List<Card> cards = availableCards.get(g);
        return cards != null && cards.contains(card);
    }

    public static Card getRandomCard(Game g, boolean miss) {
        List<Card> cards = availableCards.get(g);
        int l = cards.size();
        int[] prob = new int[l + 1];
        int sum = 0;
        for (int i = 0; i < l; i++) {
            sum += 128 / cards.get(i).getPrice(g);
            prob[i] = sum;
        }
        if (miss) {
            sum += 32;
            prob[l] = sum;
        }

        if (sum <= 0)
            return null;
        int index = Arrays.binarySearch(prob, ThreadLocalRandom.current().nextInt(sum));
        if (index < 0) {
            index = -index - 1;
        }
        if (index == l) {
            return null;
        } else {
            return cards.get(index);
        }
    }

    public static List<Card> getAvailableCards(Game g) {
        return new CopyOnWriteArrayList<>(availableCards.get(g));
    }

    private final Game game;
    private final IPlayer player;
    private final IntegerProperty coupons = new SimpleIntegerProperty(0);
    private final ObservableList<Card> cards = FXCollections.observableList(new CopyOnWriteArrayList<>());

    private Cards(IPlayer player) {
        game = player.getGame();
        this.player = player;

        if (!(player instanceof IPlayerWithCards)) {
            game.triggerException("interface not implemented: IPlayerWithCards");
        }

        game.onGameStart.addListener(() ->
                coupons.set(game.getConfig("enable-coupons")));
    }

    public IntegerProperty couponsProperty() {
        return coupons;
    }

    public final int getCoupons() {
        return coupons.get();
    }

    public final ObservableList<Card> getCards() {
        return cards;
    }

    public final int getCardsCount() {
        return cards.size();
    }

    public void useCard(Card card, Consumer0 cb) {
        synchronized (game.lock) {
            if (game.getState() == Game.State.TURN_STARTING) {
                if (cards.contains(card)) {
                    cards.remove(card);
                    onCardChange.get(game).trigger(player, false, card);
                    card.use(game, (ok) -> {
                        if (!ok) {
                            cards.add(card);
                        }
                        cb.accept();
                    });
                } else {
                    game.triggerException("you_do_not_have_this_card");
                    cb.accept();
                }
            } else {
                Logger.getAnonymousLogger().log(Level.WARNING, Game.WRONG_STATE);
            }
        }
    }

    private void _buyCards(Consumer0 cb) {
        ((IPlayerWithCards) player).askWhichCardToBuy(new Consumer<Card>() {
            @Override
            public void accept(Card card) {
                synchronized (game.lock) {
                    if (card == null) {
                        cb.accept();
                    } else {
                        int price = card.getPrice(game);
                        if (coupons.get() >= price) {
                            cards.add(card);
                            coupons.set(coupons.get() - price);
                        }
                        ((IPlayerWithCards) player).askWhichCardToBuy(this);
                    }
                }
            }
        });
    }

    public final void addCoupons(int amount) {
        synchronized (game) {
            coupons.set(coupons.get() + amount);
            onCouponChange.get(game).trigger(player, amount);
        }
    }

    public final void addCard(Card card) {
        synchronized (game.lock) {
            cards.add(card);
            onCardChange.get(game).trigger(player, true, card);
        }
    }

    public final void removeCard(Card card) {
        synchronized (game.lock) {
            if (cards.contains(card)) {
                cards.remove(card);
                onCardChange.get(game).trigger(player, false, card);
            }
        }
    }

    public final void buyCards(Consumer0 cb) {
        synchronized (game.lock) {
            _buyCards(cb);
        }
    }
}
