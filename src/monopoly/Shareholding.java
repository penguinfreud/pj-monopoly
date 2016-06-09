package monopoly;

import monopoly.extension.GameCalendar;
import monopoly.stock.Stock;
import monopoly.stock.StockMarket;
import monopoly.util.Event3;
import monopoly.util.Parasite;

import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Shareholding {
    public static class StockHolding {
        private double cost = 0.0;
        private int amount = 0;

        private StockHolding() {
        }

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

    private static final Parasite<IPlayer, Shareholding> parasites = new Parasite<>();
    public static final Parasite<Game, Event3<IPlayer, Stock, Integer>> onStockHoldingChange = new Parasite<>();

    static {
        Game.putDefaultConfig("stock-max-trade", 10000);
    }

    public static void enable(Game g) {
        if (onStockHoldingChange.get(g) == null) {
            GameCalendar.enable(g);
            onStockHoldingChange.set(g, new Event3<>());
            BasePlayer.onAddPlayer.get(g).addListener(player -> {
                Shareholding holding = new Shareholding(player);
                parasites.set(player, holding);
            });
            BasePlayer.onBankrupt.get(g).addListener(player ->
                    parasites.get(player).holdingMap.forEach((stock, holding) -> holding.amount = 0));
        }
    }

    public static boolean isEnabled(Game g) {
        return onStockHoldingChange.get(g) != null;
    }

    public static Shareholding get(IPlayer player) {
        return parasites.get(player);
    }

    private final Game game;
    private final IPlayer player;
    private final Map<Stock, StockHolding> holdingMap = new Hashtable<>();

    private Shareholding(IPlayer player) {
        this.player = player;
        game = player.getGame();
    }

    public final StockHolding get(Stock stock) {
        return holdingMap.get(stock);
    }

    public final int getAmount(Stock stock) {
        StockHolding holding = holdingMap.get(stock);
        if (holding == null) {
            return 0;
        } else {
            return holding.getAmount();
        }
    }

    public final double getAverageCost(Stock stock) {
        StockHolding holding = holdingMap.get(stock);
        if (holding == null) {
            return Double.NaN;
        } else {
            return holding.getAverageCost();
        }
    }

    public final void buy(Stock stock, int amount) {
        synchronized (game.lock) {
            if (game.getState() == Game.State.TURN_STARTING) {
                int maxTrade = game.getConfig("stock-max-trade");
                if (amount > maxTrade) {
                    game.triggerException("exceeded_max_stock_trade");
                } else if (amount < 0) {
                    game.triggerException("amount_cannot_be_negative");
                } else {
                    StockMarket market = StockMarket.getMarket(game);
                    if (market.hasStock(stock)) {
                        double price = market.getPrice(stock) * amount;
                        if (player.getCash() >= price) {
                            StockHolding holding = holdingMap.get(stock);
                            if (holding == null) {
                                holding = new StockHolding();
                                holdingMap.put(stock, holding);
                            }
                            holding.amount += amount;
                            holding.cost += price;
                            String msg = game.format("buy_stock", player.getName(), stock.toString(game), amount, price);
                            player.changeCash(-price, msg);
                            onStockHoldingChange.get(game).trigger(player, stock, amount);
                        } else {
                            game.triggerException("short_of_cash");
                        }
                    } else {
                        game.triggerException("stock_non_existent", stock.toString(game));
                    }
                }
            } else {
                Logger.getAnonymousLogger().log(Level.WARNING, Game.WRONG_STATE);
            }
        }
    }

    public final void sell(Stock stock, int amount) {
        synchronized (game.lock) {
            if (this.game.getState() == Game.State.TURN_STARTING) {
                int maxTrade = this.game.getConfig("stock-max-trade");
                if (amount > maxTrade) {
                    this.game.triggerException("exceeded_max_stock_trade");
                } else if (amount < 0) {
                    this.game.triggerException("amount_cannot_be_negative");
                } else {
                    StockMarket market = StockMarket.getMarket(game);
                    if (market.hasStock(stock)) {
                        StockHolding holding = holdingMap.get(stock);
                        if (holding == null) {
                            game.triggerException("you_have_not_bought_this_stock");
                        } else if (amount > holding.amount) {
                            game.triggerException("sell_too_much_stock");
                        } else {
                            double price = market.getPrice(stock) * amount;
                            int oldAmount = holding.amount;
                            holding.amount -= amount;
                            holding.cost = holding.cost * holding.amount / oldAmount;
                            String msg = game.format("sell_stock", player.getName(), stock.toString(game), amount, price);
                            player.changeCash(price, msg);
                            onStockHoldingChange.get(this.game).trigger(player, stock, -amount);
                        }
                    } else {
                        game.triggerException("stock_non_existent", stock.toString(game));
                    }
                }
            } else {
                Logger.getAnonymousLogger().log(Level.WARNING, Game.WRONG_STATE);
            }
        }
    }
}
