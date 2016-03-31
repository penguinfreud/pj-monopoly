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
    private static class Param {
        Game game;
        AbstractPlayer.PlaceInterface pi;

        Param(Game game, AbstractPlayer.PlaceInterface pi) {
            this.game = game;
            this.pi = pi;
        }
    }

    private static List<Callback<Param>> newsTypes = new ArrayList<>();

    static {
        Map.registerPlaceReader("News", (r, sc) -> new News());
        newsTypes.add((p) -> {
            List<AbstractPlayer> players = getSortedPlayers(p.game);
            p.pi.changeCash(players.get(0), p.game, getRandomAward(p.game), "");
        });
        newsTypes.add((p) -> {
            List<AbstractPlayer> players = getSortedPlayers(p.game);
            p.pi.changeCash(players.get(players.size() - 1), p.game, getRandomAward(p.game), "");
        });
        newsTypes.add((p) -> {
            for (AbstractPlayer player: p.game.getPlayers()) {
                p.pi.changeDeposit(player, p.game, player.getDeposit() / 10, "");
            }
        });
        newsTypes.add((p) -> {
            for (AbstractPlayer player: p.game.getPlayers()) {
                p.pi.changeDeposit(player, p.game, -player.getDeposit() / 10, "");
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
    public void onLanded(Game g, AbstractPlayer.PlaceInterface pi, Callback<Object> cb) {
        newsTypes.get(ThreadLocalRandom.current().nextInt(newsTypes.size())).run(new Param(g, pi));
        cb.run(null);
    }
}
