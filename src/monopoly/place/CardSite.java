package monopoly.place;

import monopoly.Game;
import monopoly.Cards;
import monopoly.Card;
import monopoly.util.Consumer0;

public class CardSite extends Place {
    static {
        GameMap.registerPlaceReader("CardSite", (r, sc) -> new CardSite());
    }

    private CardSite() {
        super("CardSite");
    }

    @Override
    public void arriveAt(Game g, Consumer0 cb) {
        Card card = Card.getRandomCard(g, true);
        if (card != null) {
            Cards.get(g.getCurrentPlayer()).addCard(card);
        }
        cb.run();
    }
}
