package monopoly.stock;

import monopoly.Game;
import monopoly.util.Parasite;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class StockMarket implements Serializable {
    private static final Parasite<Game, StockMarket> markets = new Parasite<>(Game::onInit, StockMarket::new);

    public static StockMarket getMarket(Game g) {
        return markets.get(g);
    }

    public static class StockTrend {
        private final List<Double> prices = new CopyOnWriteArrayList<>();

        private StockTrend(Game g) {
            Game.onTurn.addListener(g, () -> prices.add(calcNextPrice()));
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

    private final Game game;
    private final Map<Stock, StockTrend> priceMap = new Hashtable<>();

    private StockMarket(Game g) {
        game = g;
    }

    public void addStock(Stock stock) {
        if (!priceMap.containsKey(stock)) {
            priceMap.put(stock, new StockTrend(game));
        }
    }

    public boolean hasStock(Stock stock) {
        return priceMap.containsKey(stock);
    }

    public StockTrend getPrices(Stock stock) {
        return priceMap.get(stock);
    }

    public double getPrice(Stock stock, int daysAgo) {
        StockTrend prices = priceMap.get(stock);
        if (prices == null) {
            return Double.NaN;
        }
        return prices.getPrice(daysAgo);
    }

    public double getPrice(Stock stock) {
        return getPrice(stock, 0);
    }
}
