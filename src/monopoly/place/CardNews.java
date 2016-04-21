package monopoly.place;

import monopoly.Cards;
import monopoly.IPlayer;

public class CardNews {
    static {
        News.addNews(g -> {
            for (IPlayer player: g.getPlayers()) {
                Cards.get(player).addCard(Cards.getRandomCard(g, false));
            }
        });
    }

    private CardNews() {}
}
