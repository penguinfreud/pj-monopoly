package monopoly.tui;

import monopoly.*;
import monopoly.extension.BankSystem;
import monopoly.extension.Lottery;
import monopoly.place.GameMap;
import monopoly.place.GameMapReader;
import monopoly.place.Place;
import monopoly.stock.Stock;
import monopoly.stock.StockMarket;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static class Player extends BasePlayer implements IPlayerWithCardsAndStock, Properties.IPlayerWithProperties {
        Player(String name, Game g) {
            super(g);
            setName(name);
        }
    }

    private static Game game;
    private static boolean isAI = false;

    public static void main(String[] args) {
        startGame(args);
    }

    private static void startGame(String[] args) {
        for (String arg : args) {
            if (arg.equals("--ai")) {
                isAI = true;
            }
        }
        try {
            Class.forName("monopoly.tui.TUIGameMap");
            Class.forName("monopoly.tui.TUIPlace");
            Place.loadAll();
            StockMarket.addStock(new Stock("baidu"));
            StockMarket.addStock(new Stock("google"));
            StockMarket.addStock(new Stock("facebook"));
            StockMarket.addStock(new Stock("microsoft"));

            GameMap map = GameMap.readMap(Main.class.getResourceAsStream("/maps/card_rich.map"),
                    new GameMapReader() {
                        @Override
                        protected GameMap createMap() {
                            return new TUIGameMap();
                        }
                    });

            game = new Game();
            game.setMap(map);

            Properties.enable(game);
            Cards.enable(game);
            BankSystem.enable(game);
            Lottery.enable(game);
            StockMarket.enable(game);
            Shareholding.enable(game);
            Card.enableAll(game);

            if (isAI) {
                TUI.addOutput(game, System.out);
            }

            game.onGameOver.addListener(winner -> new Thread(Main::newGame).start());

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
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(game.getText("ask_player_names"));
        Scanner scanner = TUI.getScanner(System.in);
        game.addPlayer(createPlayer(scanner.nextLine()));
        game.addPlayer(createPlayer(scanner.nextLine()));
        game.start();
    }
}
