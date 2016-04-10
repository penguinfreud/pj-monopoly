package monopoly;

import monopoly.util.*;

import java.io.Serializable;
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
                Object[] buyableCards = Card.getCards().stream().filter((card) -> card.getPrice(getGame()) <= coupons).toArray();
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
            Cards cards = Cards.get(this);
            List<Card> cardList = cards.getCards();
            if (cardList.isEmpty()) {
                cb.run();
            } else {
                Card card = cardList.get(ThreadLocalRandom.current().nextInt(cardList.size()));
                cards.useCard(card, () -> startTurn(cb));
            }
        }
    }

    static {
        Game.putDefaultConfig("init-coupons", 0);
    }

    private static final Parasite<IPlayer, Cards> parasites = new Parasite<>("Cards");
    public static final Parasite<Game, Event2<IPlayer, Integer>> onCouponChange = new Parasite<>("Cards.onCouponChange");
    public static final Parasite<Game, Event3<IPlayer, Boolean, Card>> onCardChange = new Parasite<>("Cards.onCardChange");
    public static Cards get(IPlayer player) {
        return parasites.get(player);
    }

    public static void init(Game g) {
        onCouponChange.set(g, new Event2<>());
        onCardChange.set(g, new Event3<>());
        BasePlayer.onAddPlayer.get(g).addListener(player -> {
            parasites.set(player, new Cards(player));
        });
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
            coupons = game.getConfig("init-coupons"));
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
        synchronized (game.lock) {
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
