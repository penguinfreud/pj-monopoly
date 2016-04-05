package monopoly;

import monopoly.stock.Stock;
import monopoly.stock.StockMarket;

import java.util.Hashtable;
import java.util.Map;

public class Shareholding {
    public static final class Certificate {
        private Certificate() {}
    }

    private static final Certificate cert = new Certificate();

    public static class StockHolding {
        private double cost = 0.0;
        private int amount;

        private StockHolding() {}

        public double getTotalCost() {
            return cost;
        }

        public double getAverageCost() {
            return cost / amount;
        }

        public int getAmount() {
            return amount;
        }
    }

    private final Map<Stock, StockHolding> holdingMap = new Hashtable<>();

    public StockHolding getHolding(Stock stock) {
        return holdingMap.get(stock);
    }

    void buy(Game g, Stock stock, int amount) {
        StockMarket market = StockMarket.getMarket(g);
        if (market.hasStock(stock)) {
            AbstractPlayer player = g.getCurrentPlayer();
            int price = (int) market.getPrice(stock) * amount;
            if (player.getCash() >= price) {
                StockHolding holding = holdingMap.get(stock);
                if (holding == null) {
                    holding = new StockHolding();
                    holdingMap.put(stock, holding);
                }
                holding.amount += amount;
                holding.cost += price;
                String msg = g.format("buy_stock", player.getName(), stock.toString(g), amount, price);
                player.changeCash(-price, msg);
            }
        } else {
            g.triggerException("stock_non_existent", stock.toString(g));
        }
    }

    void sell(Game g, Stock stock, int amount) {
        StockMarket market = StockMarket.getMarket(g);
        if (market.hasStock(stock)) {
            AbstractPlayer player = g.getCurrentPlayer();
            StockHolding holding = holdingMap.get(stock);
            if (holding == null) {
                g.triggerException("you_have_not_bought_this_stock");
            } else if (amount > holding.amount) {
                g.triggerException("sell_too_much_stock");
            } else {
                int price = (int) market.getPrice(stock) * amount;
                int oldAmount = holding.amount;
                holding.amount -= amount;
                holding.cost = holding.cost * holding.amount / oldAmount;
                String msg = g.format("sell_stock", player.getName(), stock.toString(g), amount, price);
                player.changeCash(price, msg);
            }
        } else {
            g.triggerException("stock_non_existent", stock.toString(g));
        }
    }
}
