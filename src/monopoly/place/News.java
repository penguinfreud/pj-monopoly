package monopoly.place;

import monopoly.*;
import monopoly.util.Consumer0;
import monopoly.util.Consumer1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class News extends Place {
    private static final List<Consumer1<Game>> newsTypes = new ArrayList<>();

    static {
        GameMap.registerPlaceReader("News", (r, sc) -> new News());

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

    static void addNews(Consumer1<Game> newsType) {
        newsTypes.add(newsType);
    }

    static int getRandomAward(Game g) {
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
