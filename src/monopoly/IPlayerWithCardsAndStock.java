package monopoly;

import monopoly.stock.Stock;
import monopoly.stock.StockMarket;
import monopoly.util.Consumer1;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public interface IPlayerWithCardsAndStock extends Cards.IPlayerWithCards {
    default void askForTargetStock(Consumer1<Stock> cb) {
        ArrayList<Stock> stocks = new ArrayList<>(StockMarket.getMarket(getGame()).getStocks());
        if (stocks.isEmpty()) {
            cb.accept(null);
        } else {
            cb.accept(stocks.get(ThreadLocalRandom.current().nextInt(stocks.size())));
        }
    }
}
