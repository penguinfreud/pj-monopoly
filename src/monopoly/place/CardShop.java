package monopoly.place;

import monopoly.*;
import monopoly.util.Consumer0;

public class CardShop extends Place {
    static {
        Map.registerPlaceReader("CardShop", (r, sc) -> new CardShop());
    }

    private CardShop() {
        super("CardShop");
    }

    @Override
    public void onLanded(Game g, PlaceInterface pi, Consumer0 cb) {
        pi.buyCards(cb);
    }
}
