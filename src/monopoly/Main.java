package monopoly;

import monopoly.tui.TUIGame;
import monopoly.tui.TUIPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static TUIGame game;
    private static Scanner scanner;
    private static List<AbstractPlayer> players;

    public static void main(String[] args) throws Exception {
        Class.forName("monopoly.MapReader");
        Class.forName("monopoly.tui.TUIMap");
        Class.forName("monopoly.tui.TUIPlace");
        Class.forName("monopoly.place.Empty");
        Class.forName("monopoly.place.Land");
        Class.forName("monopoly.place.Bank");
        Class.forName("monopoly.place.News");

        Map map = Map.readMap(Main.class.getResourceAsStream("/maps/default_tui.map"));

        players = new ArrayList<>();

        game = new TUIGame();
        scanner = game.getScanner();
        game.setMap(map);

        game.onO("gameOver", (o) -> newGame());

        newGame();
    }

    private static void newGame() {
        players.clear();
        System.out.println(game.getText("ask_player_names"));
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
