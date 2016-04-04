package monopoly.place;

import monopoly.*;
import monopoly.util.Callback;

public class CardSite extends Place {
    static {
        Map.registerPlaceReader("CardSite", (r, sc) -> new CardSite());
    }

    private CardSite() {
        super("CardSite");
    }

    @Override
    public void onLanded(Game g, AbstractPlayer.PlaceInterface pi, Callback<Object> cb) {
        Card card = Card.getRandomCard(g, true);
        if (card != null) {
            pi.addCard(g.getCurrentPlayer(), g, card);
        }
        cb.run(g, null);
    }
}
