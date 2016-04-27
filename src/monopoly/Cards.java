package monopoly;

import monopoly.place.Place;
import monopoly.util.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Cards implements Serializable {
    public interface IPlayerWithCards extends IPlayer {
        default void askWhichCardToBuy(Consumer1<Card> cb) {
            int coupons = Cards.get(this).getCoupons();
            if (coupons == 0) {
                cb.run(null);
            } else {
                Object[] buyableCards = availableCards.get(getGame()).stream()
                        .filter((card) -> card.getPrice(getGame()) <= coupons).toArray();
                if (buyableCards.length == 0) {
                    cb.run(null);
                } else {
                    cb.run((Card) buyableCards[ThreadLocalRandom.current().nextInt(buyableCards.length)]);
                }
            }
        }

        default void askForTargetPlayer(String reason, Consumer1<IPlayer> cb) {
            if (reason.equals("ReverseCard")) {
                cb.run(this);
            } else {
                List<IPlayer> players = getGame().getPlayers();
                IPlayer first = players.get(0);
                cb.run(first == this? players.get(1): first);
            }
        }

        default void askForTargetPlace(String reason, Consumer1<Place> cb) {
            Place cur = getCurrentPlace();
            if (reason.equals("Roadblock")) {
                cb.run(cur);
            } else {
                cb.run(isReversed() ? cur.getPrev() : cur.getNext());
            }
        }

        default void askForInt(String reason, Consumer1<Integer> cb) {
            if (reason.equals("LotteryCard")) {
                cb.run(0);
            } else {
                cb.run(0);
            }
        }

        @Override
        default void startTurn(Consumer0 cb) {
            Cards cardsInterface = Cards.get(this);
            List<Card> cards = cardsInterface.cards;
            if (cards.isEmpty()) {
                cb.run();
            } else {
                int l = cards.size();
                cardsInterface.useCard(cards.get(l - 1), new Consumer0() {
                    private int count = l,
                    i = l - 1;
                    private Card card = cards.get(0);

                    @Override
                    public void run() {
                        if (cards.size() == count && cards.get(count - 1) == card) {
                            i--;
                        } else {
                            count = cards.size();
                        }
                        if (i < 0 || i >= count) {
                            cb.run();
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

    private static final Parasite<IPlayer, Cards> parasites = new Parasite<>("Cards");
    private static final Parasite<Game, List<Card>> availableCards = new Parasite<>("Cards.availableCards");
    public static final Parasite<Game, Event2<IPlayer, Integer>> onCouponChange = new Parasite<>("Cards.onCouponChange");
    public static final Parasite<Game, Event3<IPlayer, Boolean, Card>> onCardChange = new Parasite<>("Cards.onCardChange");
    public static Cards get(IPlayer player) {
        return parasites.get(player);
    }

    public static void enable(Game g) {
        if (availableCards.get(g) == null) {
            availableCards.set(g, new CopyOnWriteArrayList<>());
            onCouponChange.set(g, new Event2<>());
            onCardChange.set(g, new Event3<>());
            BasePlayer.onAddPlayer.get(g).addListener(player ->
                    parasites.set(player, new Cards(player)));
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
        for (int i = 0; i<l; i++) {
            sum += 128 / cards.get(i).getPrice(g);
            prob[i] = sum;
        }
        if (miss) {
            sum += 32;
            prob[l] = sum;
        }

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
    private int coupons;
    private final List<Card> cards = new CopyOnWriteArrayList<>();

    private Cards(IPlayer player) {
        game = player.getGame();
        this.player = player;

        if (!(player instanceof IPlayerWithCards)) {
            game.triggerException("interface not implemented: IPlayerWithCards");
        }

        game.onGameStart.addListener(() ->
            coupons = game.getConfig("enable-coupons"));
    }

    public final int getCoupons() {
        return coupons;
    }

    public final List<Card> getCards() {
        return new CopyOnWriteArrayList<>(cards);
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
                        cb.run();
                    });
                } else {
                    game.triggerException("you_do_not_have_this_card");
                    cb.run();
                }
            } else {
                Logger.getAnonymousLogger().log(Level.WARNING, Game.WRONG_STATE);
            }
        }
    }

    private void _buyCards(Consumer0 cb) {
        ((IPlayerWithCards) player).askWhichCardToBuy(new Consumer1<Card>() {
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
                        ((IPlayerWithCards) player).askWhichCardToBuy(this);
                    }
                }
            }
        });
    }

    public final void addCoupons(int amount) {
        synchronized (game) {
            coupons += amount;
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
