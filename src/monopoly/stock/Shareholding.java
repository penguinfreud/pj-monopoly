package monopoly.stock;

import monopoly.AbstractPlayer;
import monopoly.Game;

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

    private Map<Stock, StockHolding> holdingMap = new Hashtable<>();

    public void buy(Game g, Stock stock, int amount, AbstractPlayer.StockInterface si) {
        AbstractPlayer player = g.getCurrentPlayer();
        int price = (int) StockMarket.getMarket(g).getPrice(stock) * amount;
        if (player.getCash() >= price) {
            si.pay(player, price, "buy_stock");
        }
    }

    public void sell(Stock stock, int amount) {

    }
}
