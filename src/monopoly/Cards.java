package monopoly;

import monopoly.util.*;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Cards implements Serializable {
    public interface IPlayerWithCards {
        void askWhichCardToBuy(Consumer1<Card> cb);
        void askForTargetPlayer(String reason, Consumer1<AbstractPlayer> cb);
        void askForTargetPlace(String reason, Consumer1<Place> cb);
    }

    static {
        Game.putDefaultConfig("init-coupons", 0);
    }

    private static final Parasite<AbstractPlayer, Cards> parasites = new Parasite<>("Cards", AbstractPlayer::onInit, Cards::new);
    private static final Parasite<Game, Event2<AbstractPlayer, Integer>> _onCouponChange = new Parasite<>("Cards.onCouponChange", Game::onInit, Event2::New);
    private static final Parasite<Game, Event3<AbstractPlayer, Boolean, Card>> _onCardChange = new Parasite<>("Cards.onCardChange", Game::onInit, Event3::New);
    public static final EventWrapper<Game, Consumer2<AbstractPlayer, Integer>> onCouponChange = new EventWrapper<>(_onCouponChange);
    public static final EventWrapper<Game, Consumer3<AbstractPlayer, Boolean, Card>> onCardChange = new EventWrapper<>(_onCardChange);

    public static Cards get(AbstractPlayer player) {
        return parasites.get(player);
    }

    private final Game game;
    private final AbstractPlayer player;
    private int coupons;
    private final List<Card> cards = new CopyOnWriteArrayList<>();

    private Cards(AbstractPlayer player) {
        game = player.getGame();
        this.player = player;

        if (!(player instanceof IPlayerWithCards)) {
            game.triggerException("interface not implemented: IPlayerWithCards");
        }

        Game.onGameStart.addListener(game, () ->
            coupons = game.getConfig("init-coupons"));
    }

    public final int getCoupons() {
        return coupons;
    }

    public final List<Card> getCards() {
        return new CopyOnWriteArrayList<>(cards);
    }

    final void useCard(Card card, Consumer0 cb) {
        synchronized (game.lock) {
            if (game.getState() == Game.State.TURN_STARTING) {
                if (cards.contains(card)) {
                    cards.remove(card);
                    _onCardChange.get(game).trigger(player, false, card);
                    card.use(game, cb);
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
            _onCouponChange.get(game).trigger(player, amount);
        }
    }

    public final void addCard(Card card) {
        synchronized (game.lock) {
            cards.add(card);
            _onCardChange.get(game).trigger(player, true, card);
        }
    }

    public final void removeCard(Card card) {
        synchronized (game.lock) {
            if (cards.contains(card)) {
                cards.remove(card);
                _onCardChange.get(game).trigger(player, false, card);
            }
        }
    }

    public final void buyCards(Consumer0 cb) {
        synchronized (game.lock) {
            _buyCards(cb);
        }
    }

    public final void askForTargetPlayer(String reason, Consumer1<AbstractPlayer> cb) {
        ((IPlayerWithCards) player).askForTargetPlayer(reason, cb);
    }

    public final void askForTargetPlace(String reason, Consumer1<Place> cb) {
        ((IPlayerWithCards) player).askForTargetPlace(reason, cb);
    }
}
