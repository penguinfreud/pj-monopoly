package monopoly.place;

import monopoly.Cards;
import monopoly.Game;
import monopoly.IPlayer;
import monopoly.Properties;
import monopoly.util.Consumer0;
import monopoly.util.Consumer1;
import monopoly.util.Function1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class News extends Place {
    private static class NewsType {
        Consumer1<Game> fn;
        Function1<Game, Boolean> requirement;

        NewsType(Consumer1<Game> fn, Function1<Game, Boolean> requirement) {
            this.fn = fn;
            this.requirement = requirement;
        }
    }

    private static final List<NewsType> newsTypes = new ArrayList<>();

    static {
        GameMap.registerPlaceReader("News", (r, sc) -> new News());

        newsTypes.add(new NewsType(g -> {
            for (IPlayer player : g.getPlayers()) {
                double amount = player.getDeposit() / 10;
                String msg = g.format("news_interest", player.getName(), amount);
                player.changeDeposit(amount, msg);
            }
        }, null));
        newsTypes.add(new NewsType(g -> {
            for (IPlayer player : g.getPlayers()) {
                double amount = player.getDeposit() / 10;
                String msg = g.format("news_tax", player.getName(), amount);
                player.changeDeposit(-amount, msg);
            }
        }, null));
        newsTypes.add(new NewsType(g -> {
            for (IPlayer player : g.getPlayers()) {
                Cards.get(player).addCard(Cards.getRandomCard(g, false));
            }
        }, Cards::isEnabled));
        newsTypes.add(new NewsType(g -> {
            List<IPlayer> players = getSortedPlayers(g);
            IPlayer player = players.get(0);
            double award = News.getRandomAward(g);
            String msg = g.format("news_poorest_player", player.getName(), award);
            player.changeCash(award, msg);
        }, Properties::isEnabled));
        newsTypes.add(new NewsType(g -> {
            List<IPlayer> players = getSortedPlayers(g);
            IPlayer player = players.get(players.size() - 1);
            double award = News.getRandomAward(g);
            String msg = g.format("news_biggest_landlord", player.getName(), award);
            player.changeCash(award, msg);
        }, Properties::isEnabled));

        Game.putDefaultConfig("news-award-min", 100.0);
        Game.putDefaultConfig("news-award-max", 200.0);
    }

    private static double getRandomAward(Game g) {
        double awardMin = g.getConfig("news-award-min"),
                awardMax = g.getConfig("news-award-max");
        return ThreadLocalRandom.current().nextDouble(awardMin, awardMax);
    }

    private static List<IPlayer> getSortedPlayers(Game g) {
        List<IPlayer> players = g.getPlayers();
        Collections.sort(players, (a, b) ->
                Properties.get(a).getPropertiesCount() - Properties.get(b).getPropertiesCount());
        return players;
    }

    private News() {
        super("News");
    }

    @Override
    public void arriveAt(Game g, Consumer0 cb) {
        Object[] types = newsTypes.stream().filter(t -> t.requirement == null || t.requirement.apply(g)).toArray();
        ((NewsType) types[ThreadLocalRandom.current().nextInt(types.length)]).fn.accept(g);
        cb.accept();
    }
}
