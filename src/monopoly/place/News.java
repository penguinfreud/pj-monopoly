package monopoly.place;

import monopoly.AbstractPlayer;
import monopoly.Game;
import monopoly.Map;
import monopoly.Place;
import monopoly.async.Callback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class News extends Place {
    private static List<Callback<Game>> newsTypes = new ArrayList<>();

    static {
        Map.registerPlaceReader("News", (r, sc) -> new News());
        newsTypes.add((g) -> {
            List<AbstractPlayer> players = getSortedPlayers(g);
            players.get(0).changeCash(g, getRandomAward(g), "");
        });
        newsTypes.add((g) -> {
            List<AbstractPlayer> players = getSortedPlayers(g);
            players.get(players.size() - 1).changeCash(g, getRandomAward(g), "");
        });
        newsTypes.add((g) -> {
            for (AbstractPlayer player: g.getPlayers()) {
                player.changeDeposit(g, player.getDeposit() / 10, "");
            }
        });
        newsTypes.add((g) -> {
            for (AbstractPlayer player: g.getPlayers()) {
                player.changeDeposit(g, player.getTotalPossessions() / 10, "");
            }
        });
    }

    private static List<AbstractPlayer> getSortedPlayers(Game g) {
        List<AbstractPlayer> players = g.getPlayers();
        Collections.sort(players, (a, b) -> a.getProperties().size() - b.getProperties().size());
        return players;
    }

    private static int getRandomAward(Game g) {
        int awardMin = (Integer) g.getConfig("news-award-min"),
                awardMax = (Integer) g.getConfig("news-award-max");
        return ThreadLocalRandom.current().nextInt(awardMax - awardMin) + awardMin;
    }

    protected News() {
        super("News");
    }

    @Override
    public void onLanded(Game g, Callback<Object> cb) {

    }
}
