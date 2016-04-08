package monopoly.card;

import monopoly.Card;
import monopoly.Game;
import monopoly.IPlayerWithCardsAndStock;
import monopoly.stock.Stock;
import monopoly.stock.StockMarket;
import monopoly.util.Consumer0;
import monopoly.util.Consumer1;

public class RedCard extends Card {
    static {
        registerCard(new RedCard());
        Game.putDefaultConfig("red-card-price", 5);
    }

    private RedCard() {
        super("RedCard");
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
