package monopoly.tui;

import monopoly.*;
import monopoly.stock.Stock;
import monopoly.stock.StockMarket;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static List<IPlayer> players;
    private static TUIGame game;

    public static void main(String[] args) {
        startGame();
    }

    public static void startGame() {
        try {
            Class.forName("monopoly.GameMapReader");
            Class.forName("monopoly.tui.TUIGameMap");
            Class.forName("monopoly.tui.TUIPlace");
            Place.loadAll();
            Card.loadAll();
            Class.forName("monopoly.GameCalendar");
            Class.forName("monopoly.Shareholding");
            Class.forName("monopoly.Properties");
            Class.forName("monopoly.Cards");

            StockMarket.addStock(new Stock("baidu"));
            StockMarket.addStock(new Stock("google"));
            StockMarket.addStock(new Stock("facebook"));
            StockMarket.addStock(new Stock("microsoft"));

            GameMap map = null;
                map = GameMap.readMap(Main.class.getResourceAsStream("/maps/default_tui.map"));

            players = new ArrayList<>();

            game = new TUIGame();
            game.setMap(map);

            Game.onGameOver.addListener(game, Main::newGame);

            newGame();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void newGame() {
        players.clear();
        System.out.println(game.getText("ask_player_names"));
        Scanner scanner = game.getScanner();
        players.add(new BasePlayer(scanner.nextLine()));
        players.add(new BasePlayer(scanner.nextLine()));
        try {
            game.setPlayers(players);
            game.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
