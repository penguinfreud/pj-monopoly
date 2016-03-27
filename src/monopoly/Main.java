package monopoly;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws Exception {
        Config config = new Config();
        Map map = new Map(0);
        Game g = new Game();
        g.setMap(map);
        g.setConfig(config);
        ArrayList<AbstractPlayer> players = new ArrayList<>();
        players.add(new Player());
        players.add(new Player());
        g.setPlayers(players);
    }
}
