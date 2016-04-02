package monopoly;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class Players implements Serializable {
    private final List<AbstractPlayer> players = new CopyOnWriteArrayList<>();
    private int currentPlayerIndex = 0;

    int count() {
        return players.size();
    }

    AbstractPlayer getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    boolean isNewCycle() {
        return currentPlayerIndex == 0;
    }

    List<AbstractPlayer> getPlayers() {
        return new CopyOnWriteArrayList<>(players);
    }

    void set(List<AbstractPlayer> thePlayers) throws Exception {
        if (thePlayers.size() < 2)
            throw new Exception("Too few players.");
        players.clear();
        players.addAll(thePlayers);
        Collections.shuffle(players);
        currentPlayerIndex = 0;
    }

    void init(Game g) {
        for (AbstractPlayer player : players) {
            player.init(g);
        }
    }

    void remove(AbstractPlayer player) {
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