package monopoly.tui;

import monopoly.*;
import monopoly.Properties;
import monopoly.Card;
import monopoly.extension.GameCalendar;
import monopoly.extension.Lottery;
import monopoly.place.Place;
import monopoly.stock.Stock;
import monopoly.stock.StockMarket;
import monopoly.util.Consumer0;
import monopoly.util.Consumer1;
import monopoly.util.Function1;
import monopoly.util.Util;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class TUIPlayer extends BasePlayer implements Properties.IPlayerWithProperties, Cards.IPlayerWithCards, IPlayerWithCardsAndStock {
    private Scanner scanner;
    private PrintStream out;
    
    public TUIPlayer(Game g) {
        this("", g);
    }

    public TUIPlayer(String name, Game g) {
        this(name, g, System.in, System.out);
    }

    public TUIPlayer(String name, Game g, InputStream in, PrintStream out) {
        super(name, g);
        scanner = TUI.getScanner(in);
        TUI.addOutput(g, out);
        this.out = out;
    }

    private boolean yesOrNo(String question) {
        Game g = getGame();
        while (true) {
            out.print(question);
            String answer = scanner.nextLine().toLowerCase();
            if (answer.equals("y") || answer.equals("yes")) {
                return true;
            } else if (answer.equals("n") || answer.equals("no")) {
                return false;
            }
            out.println(g.getText("input_error"));
        }
    }

    private int getInt(String question, int min, int max, int noop) {
        Game g = getGame();
        while (true) {
            out.print(question);
            String str = scanner.nextLine();
            if (str.toLowerCase().equals("q")) {
                return noop;
            }
            try {
                int x = Integer.parseInt(str);
                if (x >= min && x <= max) {
                    return x;
                }
            } catch (NumberFormatException e) {}
            out.println(g.getText("input_error"));
        }
    }

    private double getDouble(String question, double min, double max, double noop) {
        Game g = getGame();
        while (true) {
            out.print(question);
            String str = scanner.nextLine();
            if (str.toLowerCase().equals("q")) {
                return noop;
            }
            try {
                double x = Double.parseDouble(str);
                if (x >= min && x <= max) {
                    return x;
                }
            } catch (NumberFormatException e) {}
            out.println(g.getText("input_error"));
        }
    }

    private <T> T choose(String question, List<T> options, boolean nullable, Function1<T, String> stringifier) {
        Game g = getGame();
        while (true) {
            out.println(question);
            int l = options.size();
            for (int i = 0; i<l; i++) {
                T item = options.get(i);
                String option = stringifier.run(item);
                out.println("[" + (i + 1) + "] " + option);
            }
            if (nullable) {
                out.println("[" + (l + 1) + "] " + g.getText("return"));
            }
            out.print(g.getText("please_choose"));
            String strChoice = scanner.nextLine();
            try {
                int choice = Integer.parseInt(strChoice);
                if (choice >= 1 && choice <= l) {
                    return options.get(choice - 1);
                } else if (nullable && choice == l + 1) {
                    return null;
                }
            } catch (NumberFormatException e) {}
            out.println(g.getText("input_error"));
        }
    }

    private <T extends GameObject>T choose(String question, List<T> options, boolean nullable) {
        return choose(question, options, nullable, (item) -> item.toString(getGame()));
    }

    private int chooseInt(List<String> options) {
        Game g = getGame();
        while (true) {
            int l = options.size();
            for (int i = 0; i<l; i++) {
                out.println("[" + (i + 1) + "] " + options.get(i));
            }
            out.print(g.getText("please_choose"));
            String strChoice = scanner.nextLine();
            try {
                int choice = Integer.parseInt(strChoice);
                if (choice >= 1 && choice <= l) {
                    return choice - 1;
                }
            } catch (NumberFormatException e) {}
            out.println(g.getText("input_error"));
        }
    }

    private Place nthPlace(int n) {
        Place place = getCurrentPlace();
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                place = isReversed() ? place.getPrev() : place.getNext();
            }
        } else {
            for (int i = 0; i < n; i++) {
                place = isReversed() ? place.getNext() : place.getPrev();
            }
        }
        return place;
    }

    @Override
    public void askWhetherToBuyProperty(Consumer1<Boolean> cb) {
        Property property = getCurrentPlace().asProperty();
        String question = getGame().format("ask_whether_to_buy_property", property.getName(),
                property.getPurchasePrice(), getCash());
        cb.run(yesOrNo(question));
    }

    @Override
    public void askWhetherToUpgradeProperty(Consumer1<Boolean> cb) {
        Property property = getCurrentPlace().asProperty();
        String question = getGame().format("ask_whether_to_upgrade_property", property.getName(),
                property.getUpgradePrice(), getCash());
        cb.run(yesOrNo(question));
    }

    @Override
    public void askWhichPropertyToMortgage(Consumer1<Property> cb) {
        String question = getGame().getText("ask_which_property_to_mortgage");
        cb.run(choose(question, Properties.get(this).getProperties(), false));
    }

    @Override
    public void askWhichCardToBuy(Consumer1<Card> cb) {
        Game g = getGame();
        String question = g.format("ask_which_card_to_buy", Cards.get(this).getCoupons());
        cb.run(choose(question, Cards.getAvailableCards(g), true,
                card -> g.format("card_and_price", card.toString(g), card.getPrice(g))));
    }

    private void viewMap(boolean raw) {
        Game g = getGame();
        ((TUIGameMap) g.getMap()).print(g, out, raw);
    }

    private void _askWhichCardToUse(Consumer0 cb) {
        List<Card> cards = Cards.get(this).getCards();
        if (cards.size() == 0) {
            out.println(getGame().getText("you_have_no_card"));
            startTurn(cb);
        } else {
            String question = getGame().getText("ask_which_card_to_use");
            Card card = choose(question, cards, true);
            if (card != null) {
                Cards.get(this).useCard(card, () -> _askWhichCardToUse(cb));
            } else {
                startTurn(cb);
            }
        }
    }

    private void checkAlert() {
        Game g = getGame();
        Place place = getCurrentPlace();
        boolean hasRoadblock = false;
        for (int i = 0; i<10; i++) {
            place = place.getNext();
            if (place.hasRoadblock()) {
                hasRoadblock = true;
                out.println(g.format("has_roadblock", i + 1));
            }
        }
        if (!hasRoadblock) {
            out.println(g.getText("has_no_roadblock"));
        }
    }

    private void viewPlace() {
        Game g = getGame();
        while (true) {
            int steps = getInt(g.getText("ask_which_place_to_view"), -10, 10, -11);
            if (steps == -11) {
                break;
            }
            ((TUIPlace)nthPlace(steps)).printDetail(g, out);
        }
    }

    private void viewPlayerInfo() {
        Game g = getGame();
        out.println(g.getText("player_info_table_head"));
        for (IPlayer player: g.getPlayers()) {
            Cards cards = Cards.get(player);
            out.println(g.format("player_info_table_row",
                    player.getName(),
                    player.getCash(),
                    player.getDeposit(),
                    Properties.get(player).getPropertiesCount(),
                    cards.getCoupons(),
                    cards.getCardsCount(),
                    player.getTotalPossessions()));
        }
    }

    private void menuViewStock() {
        Game g = getGame();
        Set<Map.Entry<Stock, StockMarket.StockTrend>> stocks = StockMarket.getMarket(g).getStockEntries();
        for (int i = 1; i<=5; i++) {
            out.print(i + "\t\t");
        }
        for (IPlayer player: g.getPlayers()) {
            out.print(g.format("player_holding_head", player.getName()));
        }
        out.println();
        for (Map.Entry<Stock, StockMarket.StockTrend>entry: stocks) {
            Stock stock = entry.getKey();
            StockMarket.StockTrend trend = entry.getValue();
            out.print(g.format("stock_table_row", stock.toString(g),
                    Util.formatNumber(trend.getPrice(4)),
                    Util.formatNumber(trend.getPrice(3)),
                    Util.formatNumber(trend.getPrice(2)),
                    Util.formatNumber(trend.getPrice(1)),
                    Util.formatNumber(trend.getPrice(0))));

            for (IPlayer player: g.getPlayers()) {
                out.print(g.format("player_holding_row",
                        Shareholding.get(player).getAmount(stock)));
            }
            out.println();
        }
    }

    private String formatStockItem(Map.Entry<Stock, StockMarket.StockTrend> entry) {
        Stock stock = entry.getKey();
        double price = entry.getValue().getPrice(0);
        Shareholding holding = Shareholding.get(this);
        return getGame().format("stock_item", stock.toString(getGame()), price,
                holding.getAmount(stock), Util.formatNumber(holding.getAverageCost(stock)));
    }

    private void menuBuyStock() {
        Game g = getGame();
        Set<Map.Entry<Stock, StockMarket.StockTrend>> stocks = StockMarket.getMarket(g).getStockEntries();
        while (true) {
            String question = g.format("ask_which_stock_to_buy", getCash());
            Map.Entry<Stock, StockMarket.StockTrend> choice =
                    choose(question, new CopyOnWriteArrayList<>(stocks), true, this::formatStockItem);

            if (choice == null) {
                break;
            }
            int max = g.getConfig("stock-max-trade");
            int amount = getInt(g.getText("ask_how_much_to_buy"), 0, max, -1);
            if (amount > 0) {
                Shareholding.get(this).buy(choice.getKey(), amount);
            }
        }
    }

    private void menuSellStock() {
        Game g = getGame();
        Set<Map.Entry<Stock, StockMarket.StockTrend>> stocks = StockMarket.getMarket(g).getStockEntries();
        while (true) {
            String question = g.format("ask_which_stock_to_sell", getCash());
            Map.Entry<Stock, StockMarket.StockTrend> choice =
                    choose(question, new CopyOnWriteArrayList<>(stocks), true, this::formatStockItem);
            if (choice == null) {
                break;
            }
            int max = g.getConfig("stock-max-trade");
            int amount = getInt(g.getText("ask_how_much_to_sell"), 0, max, -1);
            if (amount > 0) {
                Shareholding.get(this).sell(choice.getKey(), amount);
            }
        }
    }

    private void tradeStock() {
        loop: while (true) {
            switch (chooseInt(stockMenuItems)) {
                case 0:
                    menuViewStock();
                    break;
                case 1:
                    menuBuyStock();
                    break;
                case 2:
                    menuSellStock();
                    break;
                case 3:
                    break loop;
            }
        }
    }

    private void buyLottery() {
        Game g = getGame();
        double price = g.getConfig("lottery-price");
        if (getCash() > price) {
            int max = g.getConfig("lottery-number-max");
            String question = g.getText("ask_what_number_to_bet");
            int number = getInt(question, 0, max, -1);
            if (number >= 0) {
                Lottery.buyLottery(this, number);
            }
        }
    }

    private final List<String> gameMenuItems = new ArrayList<>();
    private final List<String> stockMenuItems = new ArrayList<>();

    {
        Game game = getGame();
        gameMenuItems.clear();
        gameMenuItems.add(game.getText("menu_view_map"));
        gameMenuItems.add(game.getText("menu_view_raw_map"));
        gameMenuItems.add(game.getText("menu_use_card"));
        gameMenuItems.add(game.getText("menu_check_alert"));
        gameMenuItems.add(game.getText("menu_view_place"));
        gameMenuItems.add(game.getText("menu_player_info"));
        gameMenuItems.add(game.getText("menu_roll_the_dice"));
        gameMenuItems.add(game.getText("menu_give_up"));
        gameMenuItems.add(game.getText("menu_stock"));
        gameMenuItems.add(game.getText("menu_buy_lottery"));
        stockMenuItems.add(game.getText("menu_view_stock"));
        stockMenuItems.add(game.getText("menu_buy_stock"));
        stockMenuItems.add(game.getText("menu_sell_stock"));
        stockMenuItems.add(game.getText("return"));
    }

    @Override
    public void startTurn(Consumer0 cb) {
        Game g = getGame();
        viewMap(false);
        String direction = g.getText(isReversed()? "anticlockwise": "clockwise");
        out.println(g.format("game_info", GameCalendar.getDate(g), getName(), direction));

        loop: while (true) {
            switch (chooseInt(gameMenuItems)) {
                case 0:
                    viewMap(false);
                    break;
                case 1:
                    viewMap(true);
                    break;
                case 2:
                    if (Cards.isEnabled(g)) {
                        _askWhichCardToUse(cb);
                        break loop;
                    } else {
                        break;
                    }
                case 3:
                    checkAlert();
                    break;
                case 4:
                    viewPlace();
                    break;
                case 5:
                    viewPlayerInfo();
                    break;
                case 6:
                    cb.run();
                    break loop;
                case 7:
                    giveUp();
                    cb.run();
                    break loop;
                case 8:
                    if (Shareholding.isEnabled(g)) {
                        tradeStock();
                    }
                    break;
                case 9:
                    if (Lottery.isEnabled(g)) {
                        buyLottery();
                    }
                    break;
            }
        }
    }

    @Override
    public void askHowMuchToDepositOrWithdraw(Consumer1<Double> cb) {
        Game g = getGame();
        String question = g.format("ask_how_much_to_deposit_or_withdraw", getCash(), getDeposit());
        cb.run(getDouble(question, -getDeposit(), getCash(), 0));
    }

    @Override
    public void askForTargetPlayer(String reason, Consumer1<IPlayer> cb) {
        Game g = getGame();
        String question;
        if (reason.equals("ReverseCard")) {
            question = g.getText("ask_whom_to_reverse");
        } else if (reason.equals("TaxCard")) {
            question = g.getText("ask_whom_to_tax");
        } else {
            question = "";
        }
        List<IPlayer> players = g.getPlayers();
        IPlayer player = choose(question, players, true);
        cb.run(player);
    }

    @Override
    public void askForTargetPlace(String reason, Consumer1<Place> cb) {
        Game g = getGame();
        if (reason.equals("ControlledDice")) {
            int steps = getInt(g.format("ask_where_to_go", getCurrentPlace().getName()), 1, 6, 0);
            if (steps == 0) {
                cb.run(null);
            } else {
                Place place = nthPlace(steps);
                cb.run(place);
            }
        } else if (reason.equals("Roadblock")) {
            int steps = getInt(g.format("ask_where_to_set_roadblock", getCurrentPlace().getName()), -8, 8, -9);
            if (steps == -9) {
                cb.run(null);
            } else {
                cb.run(nthPlace(steps));
            }
        } else {
            cb.run(null);
        }
    }

    @Override
    public void askForInt(String reason, Consumer1<Integer> cb) {
        if (reason.equals("LotteryCard")) {
            Game g = getGame();
            int max = g.getConfig("lottery-number-max");
            cb.run(getInt(g.getText("ask_for_lottery_number"), 0, max, -1));
        } else {
            cb.run(0);
        }
    }

    @Override
    public void askForTargetStock(Consumer1<Stock> cb) {
        Game g = getGame();
        StockMarket market = StockMarket.getMarket(g);
        List<Map.Entry<Stock, StockMarket.StockTrend>> stocks = new ArrayList<>(market.getStockEntries());
        Map.Entry<Stock, StockMarket.StockTrend> entry = choose(g.format("ask_for_target_stock"), stocks, true, this::formatStockItem);
        if (entry == null) {
            cb.run(null);
        } else {
            cb.run(entry.getKey());
        }
    }
}
