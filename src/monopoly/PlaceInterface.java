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
            player.changeCash(game, amount, msg);
        }
    }

    public final void changeDeposit(AbstractPlayer player, int amount, String msg) {
        synchronized (game.lock) {
            player.changeDeposit(game, amount, msg);
        }
    }

    public final void depositOrWithdraw(Consumer0 cb) {
        synchronized (game.lock) {
            game.getCurrentPlayer().depositOrWithdraw(game, cb);
        }
    }

    public final void pay(AbstractPlayer player, AbstractPlayer receiver, int amount, String msg, Consumer0 cb) {
        synchronized (game.lock) {
            player.pay(game, receiver, amount, msg, cb);
        }
    }

    public final void addCoupons(AbstractPlayer player, int amount) {
        synchronized (game.lock) {
            player.addCoupons(game, amount);
        }
    }

    public final void addCard(AbstractPlayer player, Card card) {
        synchronized (game.lock) {
            player.addCard(game, card);
        }
    }

    public final void buyCards(Consumer0 cb) {
        synchronized (game.lock) {
            game.getCurrentPlayer().buyCards(game, cb);
        }
    }
}