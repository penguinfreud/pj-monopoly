package monopoly.place;

import monopoly.Game;
import monopoly.Cards;
import monopoly.util.Consumer0;

public class CardShop extends Place {
    static {
        GameMap.registerPlaceReader("CardShop", (r, sc) -> new CardShop());
    }

    private CardShop() {
        super("CardShop");
    }

    @Override
    public void init(Game g) {
        Cards.init(g);
    }

    @Override
    public void arriveAt(Game g, Consumer0 cb) {
        Cards.get(g.getCurrentPlayer()).buyCards(cb);
    }
}
