package monopoly;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class Players {
    private final ObservableList<IPlayer> players = FXCollections.observableList(new CopyOnWriteArrayList<>());
    private int currentPlayerIndex = 0;

    int count() {
        return players.size();
    }

    IPlayer getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    boolean isNewCycle() {
        return currentPlayerIndex == 0;
    }

    ObservableList<IPlayer> getPlayers() {
        return players;
    }

    void reset() {
        players.clear();
        currentPlayerIndex = 0;
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
        if (i < currentPlayerIndex) {
            --currentPlayerIndex;
        }
        players.remove(player);
        if (currentPlayerIndex == players.size()) {
            currentPlayerIndex = 0;
        }
    }

    void next() {
        if (++currentPlayerIndex == players.size()) {
            currentPlayerIndex = 0;
        }
    }
}