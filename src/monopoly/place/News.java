package monopoly.place;

import monopoly.*;
import monopoly.util.Consumer0;
import monopoly.util.Consumer1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class News extends Place {
    private static final List<Consumer1<Game>> newsTypes = new ArrayList<>();

    static {
        GameMap.registerPlaceReader("News", (r, sc) -> new News());
        newsTypes.add(g -> {
            List<IPlayer> players = getSortedPlayers(g);
            IPlayer player = players.get(0);
            int award = getRandomAward(g);
            String msg = g.format("news_poorest_player", player.getName(), award);
            player.changeCash(award, msg);
        });
        newsTypes.add(g -> {
            List<IPlayer> players = getSortedPlayers(g);
            IPlayer player = players.get(players.size() - 1);
            int award = getRandomAward(g);
            String msg = g.format("news_biggest_landlord", player.getName(), award);
            player.changeCash(award, msg);
        });
        newsTypes.add(g -> {
            for (IPlayer player: g.getPlayers()) {
                int amount = player.getDeposit() / 10;
                String msg = g.format("news_interest", player.getName(), amount);
                player.changeDeposit(amount, msg);
            }
        });
        newsTypes.add(g -> {
            for (IPlayer player: g.getPlayers()) {
                int amount = player.getDeposit() / 10;
                String msg = g.format("news_tax", player.getName(), amount);
                player.changeDeposit(-amount, msg);
            }
        });

        Game.putDefaultConfig("news-award-min", 100);
        Game.putDefaultConfig("news-award-max", 200);
    }

    private static List<IPlayer> getSortedPlayers(Game g) {
        List<IPlayer> players = g.getPlayers();
        Collections.sort(players, (a, b) ->
                Properties.get(a).getPropertiesCount() - Properties.get(b).getPropertiesCount());
        return players;
    }

    private static int getRandomAward(Game g) {
        int awardMin = g.getConfig("news-award-min"),
                awardMax = g.getConfig("news-award-max");
        return ThreadLocalRandom.current().nextInt(awardMax - awardMin + 1) + awardMin;
    }

    private News() {
        super("News");
    }

    @Override
    public void arriveAt(Game g, Consumer0 cb) {
        newsTypes.get(ThreadLocalRandom.current().nextInt(newsTypes.size())).run(g);
        cb.run();
    }
}
