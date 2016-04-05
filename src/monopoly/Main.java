package monopoly;

import monopoly.stock.Stock;
import monopoly.stock.StockMarket;
import monopoly.tui.TUIGame;
import monopoly.tui.TUIPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static List<AbstractPlayer> players;
    private static TUIGame game;

    public static void main(String[] args) throws Exception {
        Class.forName("monopoly.MapReader");
        Class.forName("monopoly.tui.TUIMap");
        Class.forName("monopoly.tui.TUIPlace");
        Class.forName("monopoly.place.Empty");
        Class.forName("monopoly.place.Land");
        Class.forName("monopoly.place.Bank");
        Class.forName("monopoly.place.News");
        Class.forName("monopoly.place.CouponSite");
        Class.forName("monopoly.place.CardSite");
        Class.forName("monopoly.place.CardShop");
        Class.forName("monopoly.card.BuyLandCard");
        Class.forName("monopoly.card.ControlledDice");
        Class.forName("monopoly.card.ReverseCard");
        Class.forName("monopoly.card.Roadblock");
        Class.forName("monopoly.card.StayCard");
        Class.forName("monopoly.card.TaxCard");
        Class.forName("monopoly.card.GodOfLandCard");
        Class.forName("monopoly.card.TeardownCard");
        Class.forName("monopoly.card.RobCard");
        Class.forName("monopoly.card.GodOfFortuneCard");

        StockMarket.addStock(new Stock("baidu"));
        StockMarket.addStock(new Stock("google"));
        StockMarket.addStock(new Stock("facebook"));
        StockMarket.addStock(new Stock("microsoft"));

        Map map = Map.readMap(Main.class.getResourceAsStream("/maps/default_tui.map"));

        players = new ArrayList<>();

        game = new TUIGame();
        game.setMap(map);

        Game.onGameOver.addListener(game, Main::newGame);

        newGame();
    }

    private static void newGame() {
        players.clear();
        System.out.println(game.getText("ask_player_names"));
        Scanner scanner = game.getScanner();
        players.add(new TUIPlayer(scanner.nextLine()));
        players.add(new TUIPlayer(scanner.nextLine()));
        try {
            game.setPlayers(players);
            game.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
