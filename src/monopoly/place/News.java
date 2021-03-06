package monopoly.place;

import monopoly.Cards;
import monopoly.Game;
import monopoly.IPlayer;
import monopoly.Properties;
import monopoly.util.Consumer0;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;

public class News extends Place {
    private static class NewsType {
        Consumer<Game> fn;
        Function<Game, Boolean> requirement;

        NewsType(Consumer<Game> fn, Function<Game, Boolean> requirement) {
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
            IPlayer player = g.getCurrentPlayer();
            g.triggerException("news_has_moved_to_hospital", player.getName());
            player.moveToHospital();
        }, null));
        newsTypes.add(new NewsType(g -> {
            for (IPlayer player : g.getPlayers()) {
                Cards.get(player).addCard(Cards.getRandomCard(g, false));
            }
        }, Cards::isEnabled));
        newsTypes.add(new NewsType(g -> {
            IPlayer player = getPoorestPlayers(g);
            double award = News.getRandomAward(g);
            String msg = g.format("news_poorest_player", player.getName(), award);
            player.changeCash(award, msg);
        }, Properties::isEnabled));
        newsTypes.add(new NewsType(g -> {
            IPlayer player = getRichestPlayers(g);
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

    private static IPlayer getRichestPlayers(Game g) {
        return g.getPlayers().stream().reduce((a, b) -> Properties.get(a).getPropertiesCount() < Properties.get(b).getPropertiesCount() ? b : a).get();
    }

    private static IPlayer getPoorestPlayers(Game g) {
        return g.getPlayers().stream().reduce((a, b) -> Properties.get(a).getPropertiesCount() < Properties.get(b).getPropertiesCount() ? a : b).get();
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
