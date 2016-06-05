package monopoly.stock;

import monopoly.Game;
import monopoly.util.Parasite;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class StockMarket implements Serializable {
    private static final List<Stock> stocks = new CopyOnWriteArrayList<>();

    public static void addStock(Stock stock) {
        stocks.add(stock);
    }

    private static final Parasite<Game, StockMarket> markets = new Parasite<>("StockMarket");

    public static StockMarket getMarket(Game g) {
        return markets.get(g);
    }

    public static final class StockTrend {
        static {
            Game.putDefaultConfig("stock-enable-price-min", 10.0);
            Game.putDefaultConfig("stock-enable-price-max", 50.0);
            Game.putDefaultConfig("stock-max-changing-rate", 0.1);
        }

        private final Game game;
        private final List<Double> prices = new CopyOnWriteArrayList<>();
        private boolean red = false, black = false;

        private StockTrend(Game g) {
            game = g;
        }

        public double getPrice(int daysAgo) {
            int index = prices.size() - 1 - daysAgo;
            if (daysAgo >= 0 && index >= 0) {
                return prices.get(index);
            } else {
                return Double.NaN;
            }
        }

        private void initPrice() {
            double min = game.getConfig("stock-enable-price-min"),
                    max = game.getConfig("stock-enable-price-max");
            double price = ThreadLocalRandom.current().nextDouble(max - min) + min;
            prices.add(price);
        }

        private void calcNextPrice() {
            double rate = game.getConfig("stock-max-changing-rate");
            double k;
            if (red) {
                k = rate;
            } else if (black) {
                k = -rate;
            } else {
                k = ThreadLocalRandom.current().nextDouble(rate + rate) - rate;
            }
            double price = prices.get(prices.size() - 1) * (1 + k);
            prices.add(price);
            red = black = false;
        }

        private void setRed() {
            black = false;
            red = true;
        }

        private void setBlack() {
            red = false;
            black = true;
        }
    }

    public static void enable(Game g) {
        if (markets.get(g) == null) {
            markets.set(g, new StockMarket(g));
        }
    }

    public static boolean isEnabled(Game g) {
        return markets.get(g) != null;
    }

    private final Map<Stock, StockTrend> priceMap = new Hashtable<>();

    private StockMarket(Game g) {
        for (Stock stock : stocks) {
            priceMap.put(stock, new StockTrend(g));
        }
        g.onGameStart.addListener(() -> priceMap.forEach((k, v) -> v.initPrice()));
        g.onCycle.addListener(() -> priceMap.forEach((k, v) -> v.calcNextPrice()));
    }

    public final boolean hasStock(Stock stock) {
        return priceMap.containsKey(stock);
    }

    public final Set<Map.Entry<Stock, StockTrend>> getStockEntries() {
        return priceMap.entrySet();
    }

    public final Set<Stock> getStocks() {
        return priceMap.keySet();
    }

    public final StockTrend getPrices(Stock stock) {
        return priceMap.get(stock);
    }

    public final double getPrice(Stock stock, int daysAgo) {
        StockTrend prices = priceMap.get(stock);
        if (prices == null) {
            return Double.NaN;
        }
        return prices.getPrice(daysAgo);
    }

    public final double getPrice(Stock stock) {
        return getPrice(stock, 0);
    }

    public final void setRed(Stock stock) {
        priceMap.get(stock).setRed();
    }

    public final void setBlack(Stock stock) {
        priceMap.get(stock).setBlack();
    }
}
