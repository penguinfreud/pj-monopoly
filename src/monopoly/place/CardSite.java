package monopoly.place;

import monopoly.*;
import monopoly.card.Card;
import monopoly.util.Consumer0;

public class CardSite extends Place {
    static {
        Map.registerPlaceReader("CardSite", (r, sc) -> new CardSite());
    }

    private CardSite() {
        super("CardSite");
    }

    @Override
    public void onLanded(Game g, PlaceInterface pi, Consumer0 cb) {
        Card card = Card.getRandomCard(g, true);
        if (card != null) {
            pi.addCard(g.getCurrentPlayer(), card);
        }
        cb.run();
    }
}
