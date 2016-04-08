package monopoly.card;

import monopoly.Card;
import monopoly.Game;
import monopoly.IPlayerWithCardsAndStock;
import monopoly.stock.StockMarket;
import monopoly.util.Consumer0;
import monopoly.util.Consumer1;

public class BlackCard extends Card {
    static {
        registerCard(new BlackCard());
        Game.putDefaultConfig("black-card-price", 5);
    }

    private BlackCard() {
        super("BlackCard");
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
