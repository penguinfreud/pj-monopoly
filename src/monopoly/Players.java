package monopoly;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Players implements Serializable {
    private static class AbstractPlayerNode implements Serializable {
        AbstractPlayer player;
        AbstractPlayerNode next;

        AbstractPlayerNode(AbstractPlayer player) {
            this.player = player;
        }
    }

    private AbstractPlayerNode currentPlayer;
    private int _count;

    int count() {
        return _count;
    }

    AbstractPlayer getCurrentPlayer() {
        if (currentPlayer == null)
            return null;
        return currentPlayer.player;
    }

    void setPlayers(List<AbstractPlayer> players) throws Exception {
        _count = players.size();
        if (_count < 2)
            throw new Exception("Too few players.");
        Collections.shuffle(players);
        currentPlayer = new AbstractPlayerNode(players.get(0));
        AbstractPlayerNode prev = null;

        for (AbstractPlayer player: players) {
            AbstractPlayerNode node = new AbstractPlayerNode(player);
            if (prev != null)
                prev.next = node;
            prev = node;
        }
        prev.next = currentPlayer;
    }

    void removePlayer(AbstractPlayer player) {
        AbstractPlayerNode node = currentPlayer;
        do {
            if (node.player == player) {
                node.player = node.next.player;
                node.next = node.next.next;
                --_count;
                break;
            }
            node = currentPlayer.next;
        } while(node != currentPlayer);
    }

    void initPlayers(Game g) {
        AbstractPlayerNode node = currentPlayer;
        do {
            AbstractPlayer player = node.player;
            player.initPlace(g.getMap().getStartingPoint());
            player.initCash((Integer) g.getConfig("init cash"));
            player.initDeposit((Integer) g.getConfig("init deposit"));
            node = currentPlayer.next;
        } while(node != currentPlayer);
    }

    void next() {
        currentPlayer = currentPlayer.next;
    }
}
