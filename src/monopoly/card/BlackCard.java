package monopoly.card;

import monopoly.Card;
import monopoly.Cards;
import monopoly.Game;
import monopoly.IPlayerWithCardsAndStock;
import monopoly.stock.StockMarket;
import monopoly.util.Consumer0;
import monopoly.util.Consumer1;

public class BlackCard extends Card {
    private static final Card instance = new BlackCard();

    static {
        Game.putDefaultConfig("black-card-price", 5);
    }

    private BlackCard() {
        super("BlackCard");
    }

    public static void enable(Game g) {
        Cards.enableCard(g, instance);
        StockMarket.init(g);
    }

    @Override
    protected void use(Game g, Consumer1<Boolean> cb) {
        ((IPlayerWithCardsAndStock) g.getCurrentPlayer()).askForTargetStock(stock -> {
            if (stock != null) {
                StockMarket market = StockMarket.getMarket(g);
                if (market.hasStock(stock)) {
                    market.setBlack(stock);
                    cb.run(true);
                } else {
                    cb.run(false);
                }
            } else {
                cb.run(false);
            }
        });
    }
}
