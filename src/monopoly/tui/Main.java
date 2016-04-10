package monopoly.tui;

import monopoly.*;
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

    public static void main(String[] args) {
        startGame();
    }

    private static void startGame() {
        try {
            Class.forName("monopoly.tui.TUIGameMap");
            Class.forName("monopoly.tui.TUIPlace");
            Place.loadAll();
            Card.loadAll();
            StockMarket.addStock(new Stock("baidu"));
            StockMarket.addStock(new Stock("google"));
            StockMarket.addStock(new Stock("facebook"));
            StockMarket.addStock(new Stock("microsoft"));

            GameMap map = GameMap.readMap(Main.class.getResourceAsStream("/maps/card_rich.map"));

            players = new ArrayList<>();

            game = new TUIGame();
            game.setMap(map);

            game.onGameOver.addListener(Main::newGame);

            newGame();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void newGame() {
        players.clear();
        System.out.println(game.getText("ask_player_names"));
        Scanner scanner = game.getScanner();
        players.add(new TUIPlayer(scanner.nextLine(), game));
        players.add(new TUIPlayer(scanner.nextLine(), game));
        try {
            game.setPlayers(players);
            game.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
