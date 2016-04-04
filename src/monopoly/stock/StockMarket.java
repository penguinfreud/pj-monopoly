package monopoly.stock;

import monopoly.Game;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class StockMarket {
    public static class StockPrices {
        private List<Double> prices = new CopyOnWriteArrayList<>();

        private StockPrices(Game g) {
            Game.onTurn.addListener(g, (_g, o) -> prices.add(calcNextPrice()));
        }

        public double getPrice(int daysAgo) {
            int index = prices.size() - 1 - daysAgo;
            if (daysAgo >= 0 && index >= 0) {
                return prices.get(index);
            } else {
                return Double.NaN;
            }
        }

        private double calcNextPrice() {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            if (prices.isEmpty()) {
                return random.nextDouble(50.0) + 10.0;
            } else {
                double k = random.nextDouble(0.2) - 0.1;
                return prices.get(prices.size() - 1) * k;
            }
        }
    }

    private Map<Stock, StockPrices> priceMap = new Hashtable<>();

    public void addStock(Game g, Stock stock) {
        if (!priceMap.containsKey(stock)) {
            priceMap.put(stock, new StockPrices(g));
        }
    }

    public StockPrices getPrices(Stock stock) {
        return priceMap.get(stock);
    }

    public double getPrice(Stock stock, int daysAgo) {
        StockPrices prices = priceMap.get(stock);
        if (prices == null) {
            return Double.NaN;
        }
        return prices.getPrice(daysAgo);
    }
}
