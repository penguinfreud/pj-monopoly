package monopoly;

import monopoly.card.Card;
import monopoly.util.Consumer0;
import monopoly.util.Parasite;

import java.io.Serializable;

public final class PlaceInterface implements Serializable {
    static final Parasite<Game, PlaceInterface> parasites = new Parasite<>(Game::onInit, PlaceInterface::new);

    private final Game game;

    private PlaceInterface(Game g) {
        game = g;
    }

    public final void changeCash(AbstractPlayer player, int amount, String msg) {
        synchronized (game.lock) {
            player.changeCash(amount, msg);
        }
    }

    public final void changeDeposit(AbstractPlayer player, int amount, String msg) {
        synchronized (game.lock) {
            player.changeDeposit(amount, msg);
        }
    }

    public final void depositOrWithdraw(Consumer0 cb) {
        synchronized (game.lock) {
            game.getCurrentPlayer().depositOrWithdraw(cb);
        }
    }

    public final void pay(AbstractPlayer player, AbstractPlayer receiver, int amount, String msg, Consumer0 cb) {
        synchronized (game.lock) {
            player.pay(receiver, amount, msg, cb);
        }
    }

    public final void addCoupons(AbstractPlayer player, int amount) {
        synchronized (game.lock) {
            player.addCoupons(amount);
        }
    }

    public final void addCard(AbstractPlayer player, Card card) {
        synchronized (game.lock) {
            player.addCard(card);
        }
    }

    public final void buyCards(Consumer0 cb) {
        synchronized (game.lock) {
            game.getCurrentPlayer().buyCards(cb);
        }
    }
}