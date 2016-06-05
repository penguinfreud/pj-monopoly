package monopoly.place;

import monopoly.Card;
import monopoly.Cards;
import monopoly.Game;
import monopoly.util.Consumer0;

public class CardSite extends Place {
    static {
        GameMap.registerPlaceReader("CardSite", (r, sc) -> new CardSite());
    }

    private CardSite() {
        super("CardSite");
    }

    @Override
    public void init(Game g) {
        Cards.enable(g);
    }

    @Override
    public void arriveAt(Game g, Consumer0 cb) {
        Card card = Cards.getRandomCard(g, true);
        if (card != null) {
            Cards.get(g.getCurrentPlayer()).addCard(card);
        }
        cb.accept();
    }
}
