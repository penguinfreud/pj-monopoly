package monopoly;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

class Players {
    private final ObservableList<IPlayer> players = FXCollections.observableList(new CopyOnWriteArrayList<>());
    private IntegerProperty currentPlayerIndex = new SimpleIntegerProperty(0);
    private ObjectBinding<IPlayer> _currentPlayer = Bindings.createObjectBinding(() -> {
        int i = currentPlayerIndex.get();
        return i < players.size() ?
                players.get(i) : null;
    }, players, currentPlayerIndex);
    ;

    int count() {
        return players.size();
    }

    ObjectBinding<IPlayer> currentPlayer() {
        return _currentPlayer;
    }

    boolean isNewCycle() {
        return currentPlayerIndex.get() == 0;
    }

    ObservableList<IPlayer> getPlayers() {
        return players;
    }

    void reset() {
        players.clear();
        currentPlayerIndex.set(0);
    }

    void add(IPlayer player) {
        players.add(player);
    }

    void shuffle() {
        Collections.shuffle(players);
    }

    void init() {
        players.forEach(IPlayer::init);
    }

    void remove(IPlayer player) {
        int i = players.indexOf(player);
        if (i == -1) return;
        if (i < currentPlayerIndex.get()) {
            currentPlayerIndex.set(currentPlayerIndex.get() - 1);
        }
        players.remove(player);
        if (currentPlayerIndex.get() == players.size()) {
            currentPlayerIndex.set(0);
        }
    }

    void next() {
        int i = currentPlayerIndex.get();
        if (i + 1 == players.size()) {
            currentPlayerIndex.set(0);
        } else {
            currentPlayerIndex.set(i + 1);
        }
    }
}