package monopoly;

import monopoly.card.Card;
import monopoly.util.Consumer0;
import monopoly.util.Parasite;
import monopoly.util.SerializableObject;

import java.io.Serializable;

public final class CardInterface implements Serializable {
    static final Parasite<Game, CardInterface> parasites = new Parasite<>(Game::onInit, CardInterface::new);

    public final SerializableObject lock;
    private final Game game;

    private CardInterface(Game g) {
        lock = g.lock;
        game = g;
    }

    public final void reverse(AbstractPlayer player) {
        synchronized (lock) {
            player.reverse();
        }
    }

    public final void walk(int steps) {
        synchronized (lock) {
            if (steps >= 0 && steps <= (Integer) game.getConfig("dice-sides")) {
                game.startWalking(steps);
            } else {
                game.triggerException("invalid_steps");
            }
        }
    }

    public final void setRoadblock(Place place) {
        synchronized (lock) {
            place.setRoadblock();
        }
    }

    public final void buyProperty(Consumer0 cb) {
        synchronized (lock) {
            game.getCurrentPlayer().buyProperty(cb, true);
        }
    }

    public final void changeCash(AbstractPlayer player, int amount, String msg) {
        synchronized (lock) {
            player.changeCash(amount, msg);
        }
    }

    public final void changeDeposit(AbstractPlayer player, int amount, String msg) {
        synchronized (lock) {
            player.changeDeposit(amount, msg);
        }
    }

    public final void robLand() {
        synchronized (lock) {
            game.getCurrentPlayer().robLand();
        }
    }

    public final void resetLevel(Property prop) {
        synchronized (lock) {
            prop.resetLevel();
        }
    }

    public final void resetOwner(Property prop) {
        synchronized (lock) {
            prop.resetOwner();
        }
    }

    public final void addCard(AbstractPlayer player, Card card) {
        synchronized (lock) {
            player.addCard(card);
        }
    }

    public final void removeCard(AbstractPlayer player, Card card) {
        synchronized (lock) {
            player.removeCard(card);
        }
    }

    public final void setRentFree(AbstractPlayer player) {
        synchronized (lock) {
            player.setRentFree();
        }
    }
}
