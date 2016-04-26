package monopoly.place;

import monopoly.Game;
import monopoly.IPlayer;
import monopoly.Properties;

import java.util.Collections;
import java.util.List;

public class PropertyNews {
    static {
        News.addNews(g -> {
            List<IPlayer> players = getSortedPlayers(g);
            IPlayer player = players.get(0);
            double award = News.getRandomAward(g);
            String msg = g.format("news_poorest_player", player.getName(), award);
            player.changeCash(award, msg);
        });
        News.addNews(g -> {
            List<IPlayer> players = getSortedPlayers(g);
            IPlayer player = players.get(players.size() - 1);
            double award = News.getRandomAward(g);
            String msg = g.format("news_biggest_landlord", player.getName(), award);
            player.changeCash(award, msg);
        });
    }

    private static List<IPlayer> getSortedPlayers(Game g) {
        List<IPlayer> players = g.getPlayers();
        Collections.sort(players, (a, b) ->
                Properties.get(a).getPropertiesCount() - Properties.get(b).getPropertiesCount());
        return players;
    }

    private PropertyNews() {}
}
