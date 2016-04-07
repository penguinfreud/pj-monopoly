package monopoly.place;

import monopoly.Card;
import monopoly.Cards;
import monopoly.IPlayer;

public class CardNews {
    static {
        News.addNews(g -> {
            for (IPlayer player: g.getPlayers()) {
                Cards.get(player).addCard(Card.getRandomCard(g, false));
            }
        });
    }

    private CardNews() {}
}
