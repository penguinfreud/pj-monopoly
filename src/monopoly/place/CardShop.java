package monopoly.place;

import monopoly.AbstractPlayer;
import monopoly.Game;
import monopoly.Map;
import monopoly.Place;
import monopoly.util.Consumer0;

public class CardShop extends Place {
    static {
        Map.registerPlaceReader("CardShop", (r, sc) -> new CardShop());
    }

    private CardShop() {
        super("CardShop");
    }

    @Override
    public void onLanded(Game g, AbstractPlayer.PlaceInterface pi, Consumer0 cb) {
        pi.buyCards(cb);
    }
}
