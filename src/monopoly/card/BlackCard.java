package monopoly.card;

import monopoly.Card;
import monopoly.Game;
import monopoly.IPlayerWithCardsAndStock;
import monopoly.stock.StockMarket;
import monopoly.util.Consumer0;

public class BlackCard extends Card {
    static {
        registerCard(new BlackCard());
        Game.putDefaultConfig("black-card-price", 5);
    }

    private BlackCard() {
        super("BlackCard");
    }

    @Override
    protected void use(Game g, Consumer0 cb) {
        ((IPlayerWithCardsAndStock) g.getCurrentPlayer()).askForTargetStock(stock -> {
            if (stock != null) {
                StockMarket.getMarket(g).setBlack(stock);
            }
            cb.run();
        });
    }
}
