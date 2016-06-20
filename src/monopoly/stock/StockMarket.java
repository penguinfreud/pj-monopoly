package monopoly.stock;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import monopoly.Game;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class StockMarket {
    private static final Map<Game, StockMarket> markets = new Hashtable<>();

    public static StockMarket getMarket(Game g) {
        return markets.get(g);
    }

    static {
        Game.putDefaultConfig("stock-enable-price-min", 10.0);
        Game.putDefaultConfig("stock-enable-price-max", 50.0);
        Game.putDefaultConfig("stock-max-changing-rate", 0.1);
    }

    public final class StockTrend {
        private final Game game;
        private final ObservableList<Double> prices = FXCollections.observableList(new CopyOnWriteArrayList<>());
        private boolean red = false, black = false;

        private StockTrend(Game g) {
            game = g;
        }

        public DoubleBinding getPrice(int daysAgo) {
            return Bindings.createDoubleBinding(() -> {
                if (daysAgo < prices.size())
                    return prices.get(daysAgo);
                return 0.0;
            }, prices);
        }

        private void initPrice() {
            double min = game.getConfig("stock-enable-price-min"),
                    max = game.getConfig("stock-enable-price-max");
            double price = ThreadLocalRandom.current().nextDouble(max - min) + min;
            prices.clear();
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
            double price = prices.get(0) * (1 + k);
            prices.add(0, price);
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
            markets.put(g, new StockMarket(g));
        }
    }

    public static boolean isEnabled(Game g) {
        return markets.get(g) != null;
    }

    private final ObservableList<Stock> stocks = FXCollections.observableList(new CopyOnWriteArrayList<>());
    private final Map<Stock, StockTrend> priceMap = new Hashtable<>();

    public void addStock(Stock stock) {
        stocks.add(stock);
    }

    public ObservableList<Stock> getStocks() {
        return stocks;
    }

    private StockMarket(Game g) {
        g.onGameStart.addListener(() -> {
            for (Stock stock : stocks) {
                priceMap.put(stock, new StockTrend(g));
            }
            priceMap.forEach((k, v) -> v.initPrice());
        });
        g.onCycle.addListener(() -> priceMap.forEach((k, v) -> v.calcNextPrice()));
    }

    public final boolean hasStock(Stock stock) {
        return priceMap.containsKey(stock);
    }

    public final Set<Map.Entry<Stock, StockTrend>> getStockEntries() {
        return priceMap.entrySet();
    }

    public final StockTrend getPrices(Stock stock) {
        return priceMap.get(stock);
    }

    public final DoubleBinding getPrice(Stock stock, int daysAgo) {
        StockTrend prices = priceMap.get(stock);
        if (prices == null) {
            return null;
        }
        return prices.getPrice(daysAgo);
    }

    public final DoubleBinding getPrice(Stock stock) {
        return getPrice(stock, 0);
    }

    public final void setRed(Stock stock) {
        priceMap.get(stock).setRed();
    }

    public final void setBlack(Stock stock) {
        priceMap.get(stock).setBlack();
    }
}
