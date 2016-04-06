package monopoly.place;

import monopoly.*;
import monopoly.util.Consumer0;
import monopoly.util.Consumer2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class News extends Place {
    private static final List<Consumer2<Game, PlaceInterface>> newsTypes = new ArrayList<>();

    static {
        Map.registerPlaceReader("News", (r, sc) -> new News());
        newsTypes.add((g, pi) -> {
            List<AbstractPlayer> players = getSortedPlayers(g);
            AbstractPlayer player = players.get(0);
            int award = getRandomAward(g);
            String msg = g.format("news_poorest_player", player.getName(), award);
            pi.changeCash(player, award, msg);
        });
        newsTypes.add((g, pi) -> {
            List<AbstractPlayer> players = getSortedPlayers(g);
            AbstractPlayer player = players.get(players.size() - 1);
            int award = getRandomAward(g);
            String msg = g.format("news_biggest_landlord", player.getName(), award);
            pi.changeCash(player, award, msg);
        });
        newsTypes.add((g, pi) -> {
            for (AbstractPlayer player: g.getPlayers()) {
                int amount = player.getDeposit() / 10;
                String msg = g.format("news_interest", player.getName(), amount);
                pi.changeDeposit(player, amount, msg);
            }
        });
        newsTypes.add((g, pi) -> {
            for (AbstractPlayer player: g.getPlayers()) {
                int amount = player.getDeposit() / 10;
                String msg = g.format("news_tax", player.getName(), amount);
                pi.changeDeposit(player, -amount, msg);
            }
        });

        Game.putDefaultConfig("news-award-min", 100);
        Game.putDefaultConfig("news-award-max", 200);
    }

    private static List<AbstractPlayer> getSortedPlayers(Game g) {
        List<AbstractPlayer> players = g.getPlayers();
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
    public void onLanded(Game g, PlaceInterface pi, Consumer0 cb) {
        newsTypes.get(ThreadLocalRandom.current().nextInt(newsTypes.size())).run(g, pi);
        cb.run();
    }
}
