package monopoly;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class Players implements Serializable {
    private final List<IPlayer> players = new CopyOnWriteArrayList<>();
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

    List<IPlayer> getPlayers() {
        return new CopyOnWriteArrayList<>(players);
    }

    void set(List<IPlayer> thePlayers) throws Exception {
        if (thePlayers.size() < 2)
            throw new Exception("Too few players.");
        players.clear();
        players.addAll(thePlayers);
        currentPlayerIndex = 0;
    }

    void shuffle() {
        Collections.shuffle(players);
    }

    void init(Game g) {
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