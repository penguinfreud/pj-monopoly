package monopoly.tui;

import monopoly.*;
import monopoly.Properties;
import monopoly.Card;
import monopoly.stock.Stock;
import monopoly.stock.StockMarket;
import monopoly.util.Consumer0;
import monopoly.util.Consumer1;
import monopoly.util.Function1;

import java.text.DecimalFormat;
import java.util.*;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class TUIPlayer extends BasePlayer implements Properties.IPlayerWithProperties, Cards.IPlayerWithCards {
    public TUIPlayer() {}

    public TUIPlayer(String name) {
        setName(name);
    }

    private boolean yesOrNo(String question) {
        Game g = getGame();
        while (true) {
            System.out.print(question);
            String answer = ((TUIGame) g).getScanner().nextLine().toLowerCase();
            if (answer.equals("y") || answer.equals("yes")) {
                return true;
            } else if (answer.equals("n") || answer.equals("no")) {
                return false;
            }
            System.out.println(g.getText("input_error"));
        }
    }

    private int getInt(String question, int min, int max, int noop) {
        TUIGame g = (TUIGame) getGame();
        while (true) {
            System.out.print(question);
            String str = g.getScanner().nextLine();
            if (str.toLowerCase().equals("q")) {
                return noop;
            }
            try {
                int x = Integer.parseInt(str);
                if (x >= min && x <= max) {
                    return x;
                }
            } catch (NumberFormatException e) {}
            System.out.println(g.getText("input_error"));
        }
    }

    private <T> T choose(String question, List<T> options, boolean nullable, Function1<T, String> stringifier) {
        TUIGame g = (TUIGame) getGame();
        while (true) {
            System.out.println(question);
            int l = options.size();
            for (int i = 0; i<l; i++) {
                T item = options.get(i);
                String option = stringifier.run(item);
                System.out.println("[" + (i + 1) + "] " + option);
            }
            if (nullable) {
                System.out.println("[" + (l + 1) + "] " + g.getText("return"));
            }
            System.out.print(g.getText("please_choose"));
            String strChoice = g.getScanner().nextLine();
            try {
                int choice = Integer.parseInt(strChoice);
                if (choice >= 1 && choice <= l) {
                    return options.get(choice - 1);
                } else if (nullable && choice == l + 1) {
                    return null;
                }
            } catch (NumberFormatException e) {}
            System.out.println(g.getText("input_error"));
        }
    }

    private <T extends GameObject>T choose(String question, List<T> options, boolean nullable) {
        return choose(question, options, nullable, (item) -> item.toString(getGame()));
    }

    private int chooseInt(List<String> options) {
        TUIGame g = (TUIGame) getGame();
        while (true) {
            int l = options.size();
            for (int i = 0; i<l; i++) {
                System.out.println("[" + (i + 1) + "] " + options.get(i));
            }
            System.out.print(g.getText("please_choose"));
            String strChoice = g.getScanner().nextLine();
            try {
                int choice = Integer.parseInt(strChoice);
                if (choice >= 1 && choice <= l) {
                    return choice - 1;
                }
            } catch (NumberFormatException e) {}
            System.out.println(g.getText("input_error"));
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
        String question = getGame().format("ask_whether_to_buy_property", property.getName(), property.getPurchasePrice(), getCash());
        cb.run(yesOrNo(question));
    }

    @Override
    public void askWhetherToUpgradeProperty(Consumer1<Boolean> cb) {
        Property property = getCurrentPlace().asProperty();
        String question = getGame().format("ask_whether_to_upgrade_property", property.getName(), property.getUpgradePrice(), getCash());
        cb.run(yesOrNo(question));
    }

    @Override
    public void askWhichPropertyToMortgage(Consumer1<Property> cb) {
        String question = getGame().getText("ask_which_property_to_mortgage");
        cb.run(choose(question, Properties.get(this).getProperties(), false));
    }

    @Override
    public void askWhichCardToBuy(Consumer1<Card> cb) {
        String question = getGame().format("ask_which_card_to_buy", Cards.get(this).getCoupons());
        cb.run(choose(question, Card.getCards(), true));
    }

    private void viewMap(Game g, boolean raw) {
        ((TUIGameMap) g.getMap()).print(g, System.out, raw);
    }

    private void _askWhichCardToUse() {
        List<Card> cards = Cards.get(this).getCards();
        if (cards.size() == 0) {
            System.out.println(getGame().getText("you_have_no_card"));
        } else {
            String question = getGame().getText("ask_which_card_to_use");
            Card card = choose(question, cards, true);
            if (card != null) {
                useCard(card, this::_askWhichCardToUse);
            }
        }
    }

    private void checkAlert(Game g) {
        Place place = getCurrentPlace();
        boolean hasRoadblock = false;
        for (int i = 0; i<10; i++) {
            place = place.getNext();
            if (place.hasRoadblock()) {
                hasRoadblock = true;
                System.out.println(g.format("has_roadblock", i + 1));
            }
        }
        if (!hasRoadblock) {
            System.out.println(g.getText("has_no_roadblock"));
        }
    }

    private void viewPlace() {
        Game g = getGame();
        while (true) {
            int steps = getInt(g.getText("ask_which_place_to_view"), 0, 10, -1);
            if (steps == -1) {
                break;
            }
            ((TUIPlace)nthPlace(steps)).printDetail(g, System.out);
        }
    }

    private void viewPlayerInfo() {
        Game g = getGame();
        System.out.println(g.getText("player_info_table_head"));
        for (IPlayer player: g.getPlayers()) {
            System.out.println(g.format("player_info_table_row",
                    player.getCash(),
                    player.getDeposit(),
                    Properties.get(player).getPropertiesCount(),
                    Cards.get(player).getCoupons(),
                    player.getTotalPossessions()));
        }
    }

    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    private String formatStockPrice(double price) {
        if (Double.isNaN(price)) {
            return getGame().getText("n/a");
        } else {
            return df.format(price);
        }
    }

    private void menuViewStock() {
        Game g = getGame();
        Set<Map.Entry<Stock, StockMarket.StockTrend>> stocks = StockMarket.getMarket(g).getStockEntries();
        for (Map.Entry<Stock, StockMarket.StockTrend>entry: stocks) {
            StockMarket.StockTrend trend = entry.getValue();
            System.out.println(g.format("stock_table_row", entry.getKey().toString(g),
                    formatStockPrice(trend.getPrice(-4)),
                    formatStockPrice(trend.getPrice(-3)),
                    formatStockPrice(trend.getPrice(-2)),
                    formatStockPrice(trend.getPrice(-1)),
                    formatStockPrice(trend.getPrice(0))));
        }
    }

    private String formatStockItem(Map.Entry<Stock, StockMarket.StockTrend> entry) {
        Stock stock = entry.getKey();
        double price = entry.getValue().getPrice(0);
        return getGame().format("stock_item", stock.toString(getGame()), formatStockPrice(price),
                Shareholding.get(this).getHolding(stock));
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
        int price = g.getConfig("lottery-price");
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

    @Override
    public void setGame(Game game) {
        super.setGame(game);
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
        viewMap(g, false);
        String direction = g.getText(isReversed()? "anticlockwise": "clockwise");
        System.out.println(g.format("game_info", GameCalendar.getDate(g), getName(), direction));

        loop: while (true) {
            switch (chooseInt(gameMenuItems)) {
                case 0:
                    viewMap(g, false);
                    break;
                case 1:
                    viewMap(g, true);
                    break;
                case 2:
                    _askWhichCardToUse();
                    break loop;
                case 3:
                    checkAlert(g);
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
                    tradeStock();
                    break;
                case 9:
                    buyLottery();
                    break;
            }
        }
    }

    @Override
    public void askHowMuchToDepositOrWithdraw(Consumer1<Integer> cb) {
        Game g = getGame();
        int maxTransfer = g.getConfig("bank-max-transfer");
        String question = g.format("ask_how_much_to_deposit_or_withdraw", getCash(), getDeposit());
        cb.run(getInt(question, -maxTransfer, maxTransfer, 0));
    }

    @Override
    public void askForTargetPlayer(String reason, Consumer1<IPlayer> cb) {
        Game g = getGame();
        if (reason.equals("ReverseCard")) {
            String question = g.getText("ask_whom_to_reverse");
            List<IPlayer> players = g.getPlayers();
            IPlayer player = choose(question, players, true);
            cb.run(player);
        } else {
            cb.run(null);
        }
    }

    @Override
    public void askForTargetPlace(String reason, Consumer1<Place> cb) {
        Game g = getGame();
        if (reason.equals("ControlledDice")) {
            int steps = getInt(g.format("ask_where_to_go", getCurrentPlace().getName()), 1, 6, 0);
            if (steps == 0) {
                cb.run(null);
            }
            Place place = nthPlace(steps);
            cb.run(place);
        } else if (reason.equals("Roadblock")) {
            int steps = getInt(g.format("ask_where_to_set_roadblock", getCurrentPlace().getName()), -8, 8, -9);
            if (steps == -9) {
                cb.run(null);
            }
            cb.run(nthPlace(steps));
        } else {
            cb.run(null);
        }
    }
}
