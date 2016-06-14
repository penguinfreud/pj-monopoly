package monopoly;

import monopoly.stock.Stock;
import monopoly.stock.StockMarket;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public interface IPlayerWithCardsAndStock extends Cards.IPlayerWithCards {
    default void askForTargetStock(Card card, Consumer<Stock> cb) {
        ArrayList<Stock> stocks = new ArrayList<>(StockMarket.getMarket(getGame()).getStocks());
        if (stocks.isEmpty()) {
            cb.accept(null);
        } else {
            cb.accept(stocks.get(ThreadLocalRandom.current().nextInt(stocks.size())));
        }
    }
}
