package monopoly.card;

import monopoly.*;
import monopoly.stock.Stock;
import monopoly.stock.StockMarket;
import monopoly.util.Consumer0;
import monopoly.util.Consumer1;

public class RedCard extends Card {
    private static final Card instance = new RedCard();
    static {
        Game.putDefaultConfig("red-card-price", 5);
    }

    private RedCard() {
        super("RedCard");
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
                    StockMarket.getMarket(g).setRed(stock);
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
