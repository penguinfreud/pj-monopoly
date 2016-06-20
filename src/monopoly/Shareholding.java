package monopoly;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import monopoly.extension.GameCalendar;
import monopoly.stock.Stock;
import monopoly.stock.StockMarket;
import monopoly.util.Event3;

import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Shareholding {
    public static class StockHolding {
        private final DoubleProperty cost = new SimpleDoubleProperty(0.0);
        private final IntegerProperty amount = new SimpleIntegerProperty(0);

        private StockHolding() {
        }

        public DoubleBinding averageCost() {
            return cost.divide(amount);
        }

        public IntegerProperty getAmount() {
            return amount;
        }
    }

    private static final Map<IPlayer, Shareholding> parasites = new Hashtable<>();
    public static final Map<Game, Event3<IPlayer, Stock, Integer>> onStockHoldingChange = new Hashtable<>();

    static {
        Game.putDefaultConfig("stock-max-trade", 10000);
    }

    public static void enable(Game g) {
        if (onStockHoldingChange.get(g) == null) {
            GameCalendar.enable(g);
            onStockHoldingChange.put(g, new Event3<>());
            g.onGameOver.addListener(winner -> parasites.clear());
            BasePlayer.onAddPlayer.get(g).addListener(player -> {
                Shareholding holding = new Shareholding(player);
                parasites.put(player, holding);
            });
            BasePlayer.onBankrupt.get(g).addListener(player ->
                    parasites.get(player).holdingMap.forEach((stock, holding) -> holding.amount.set(0)));
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
    private final ObservableMap<Stock, StockHolding> holdingMap = FXCollections.observableMap(new Hashtable<>());

    private Shareholding(IPlayer player) {
        this.player = player;
        game = player.getGame();
    }

    public final StockHolding get(Stock stock) {
        return holdingMap.get(stock);
    }

    public final IntegerProperty getAmount(Stock stock) {
        StockHolding holding = holdingMap.get(stock);
        if (holding == null) {
            holding = new StockHolding();
            holdingMap.put(stock, holding);
        }
        return holding.amount;
    }

    public final DoubleBinding getAverageCost(Stock stock) {
        StockHolding holding = holdingMap.get(stock);
        if (holding == null) {
            holding = new StockHolding();
            holdingMap.put(stock, holding);
        }
        return holding.averageCost();
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
                        double price = market.getPrice(stock).get() * amount;
                        if (player.getCash() >= price) {
                            StockHolding holding = holdingMap.get(stock);
                            if (holding == null) {
                                holding = new StockHolding();
                                holdingMap.put(stock, holding);
                            }
                            holding.amount.set(holding.amount.get() + amount);
                            holding.cost.set(holding.cost.get() + price);
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
                        } else if (amount > holding.amount.get()) {
                            game.triggerException("sell_too_much_stock");
                        } else {
                            double price = market.getPrice(stock).get() * amount;
                            int oldAmount = holding.amount.get();
                            holding.amount.set(holding.amount.get() - amount);
                            holding.cost.set(holding.cost.get() * holding.amount.get() / oldAmount);
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
