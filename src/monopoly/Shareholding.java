package monopoly;

import monopoly.stock.Stock;
import monopoly.stock.StockMarket;
import monopoly.util.Consumer3;
import monopoly.util.Event3;
import monopoly.util.EventWrapper;
import monopoly.util.Parasite;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Shareholding implements Serializable {
    static class StockHolding implements Serializable {
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

    private static final Parasite<IPlayer, Shareholding> parasites = new Parasite<>("Shareholding", BasePlayer::onInit, Shareholding::new);
    private static final Parasite<Game, Event3<IPlayer, Stock, Integer>> _onStockHoldingChange = new Parasite<>("Shareholding.onStockHoldingChange", Game::onInit, Event3::New);
    public static final EventWrapper<Game, Consumer3<IPlayer, Stock, Integer>> onStockHoldingChange = new EventWrapper<>(_onStockHoldingChange);

    public static Shareholding get(IPlayer player) {
        return parasites.get(player);
    }

    static {
        Game.putDefaultConfig("stock-max-trade", 10000);
        BasePlayer.addPossession(player -> get(player).getValue());
    }

    private final Game game;
    private final IPlayer player;
    private final Map<Stock, StockHolding> holdingMap = new Hashtable<>();

    private Shareholding(IPlayer player) {
        this.player = player;
        game = player.getGame();
    }

    private final int getValue() {
        return (int)(double) (holdingMap.entrySet().stream().map(Map.Entry::getValue)
                .map(StockHolding::getTotalCost).reduce(0.0, (a, b) -> a + b));
    }

    public final StockHolding getHolding(Stock stock) {
        return holdingMap.get(stock);
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
                        int price = (int) market.getPrice(stock) * amount;
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
                            _onStockHoldingChange.get(game).trigger(player, stock, amount);
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
                            int price = (int) market.getPrice(stock) * amount;
                            int oldAmount = holding.amount;
                            holding.amount -= amount;
                            holding.cost = holding.cost * holding.amount / oldAmount;
                            String msg = game.format("sell_stock", player.getName(), stock.toString(game), amount, price);
                            player.changeCash(price, msg);
                            _onStockHoldingChange.get(this.game).trigger(player, stock, -amount);
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
