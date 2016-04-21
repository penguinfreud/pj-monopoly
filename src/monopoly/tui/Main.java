package monopoly.tui;

import monopoly.*;
import monopoly.place.GameMap;
import monopoly.place.Place;
import monopoly.stock.Stock;
import monopoly.stock.StockMarket;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static class Player extends BasePlayer implements IPlayerWithCardsAndStock, Properties.IPlayerWithProperties {
        Player(String name, Game g) {
            super(name, g);
        }
    }

    private static List<IPlayer> players;
    private static TUIGame game;
    private static boolean isAI = false;

    public static void main(String[] args) {
        startGame(args);
    }

    private static void startGame(String[] args) {
        for (String arg: args) {
            if (arg.equals("--ai")) {
                isAI = true;
            }
        }
        try {
            Class.forName("monopoly.tui.TUIGameMap");
            Class.forName("monopoly.tui.TUIPlace");
            Place.loadAll();
            Class.forName("monopoly.place.PropertyNews");
            Class.forName("monopoly.place.CardNews");
            StockMarket.addStock(new Stock("baidu"));
            StockMarket.addStock(new Stock("google"));
            StockMarket.addStock(new Stock("facebook"));
            StockMarket.addStock(new Stock("microsoft"));

            GameMap map = GameMap.readMap(Main.class.getResourceAsStream("/maps/default_tui.map"));

            players = new ArrayList<>();

            game = new TUIGame();
            game.setMap(map);

            game.onGameOver.addListener(Main::newGame);

            newGame();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static IPlayer createPlayer(String name) {
        if (isAI) {
            return new Player(name, game);
        } else {
            return new TUIPlayer(name, game);
        }
    }

    private static void newGame() {
        players.clear();
        System.out.println(game.getText("ask_player_names"));
        Scanner scanner = game.getScanner();
        players.add(createPlayer(scanner.nextLine()));
        players.add(createPlayer(scanner.nextLine()));
        try {
            game.setPlayers(players);
            game.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
