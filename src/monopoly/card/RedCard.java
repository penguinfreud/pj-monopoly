package monopoly.card;

import monopoly.Card;
import monopoly.Game;
import monopoly.IPlayerWithCardsAndStock;
import monopoly.stock.StockMarket;
import monopoly.util.Consumer0;

public class RedCard extends Card {
    static {
        registerCard(new RedCard());
        Game.putDefaultConfig("red-card-price", 5);
    }

    private RedCard() {
        super("RedCard");
    }

    @Override
    protected void use(Game g, Consumer0 cb) {
        ((IPlayerWithCardsAndStock) g.getCurrentPlayer()).askForTargetStock(stock -> {
            if (stock != null) {
                StockMarket.getMarket(g).setRed(stock);
            }
            cb.run();
        });
    }
}
