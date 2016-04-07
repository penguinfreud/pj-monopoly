package monopoly;

import monopoly.util.Consumer0;
import monopoly.util.Consumer1;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GameSerializationTest {
    private Game game = new Game();
    
    private static class Player extends BasePlayer implements Properties.IPlayerWithProperties, IPlayerWithCardsAndStock {
        private Consumer0 cb;
        private Consumer1<Integer> cbi;
        private Consumer1<Boolean> cbb;
        private Consumer1<Property> cbpr;
        private Consumer1<Place> cbpl;
        private Consumer1<Card> cbc;
        private Consumer1<IPlayer> cba;
        private String reason;

        public Player(String name) {
            setName(name);
        }

        @Override
        public void startTurn(Consumer0 cb) {
            this.cb = cb;
            serialize(getGame(), 1);
            super.startTurn(cb);
        }

        @Override
        public void askHowMuchToDepositOrWithdraw(Consumer1<Integer> cb) {
            cbi = cb;
            serialize(getGame(), 2);
            super.askHowMuchToDepositOrWithdraw(cb);
        }

        @Override
        public void askWhichCardToBuy(Consumer1<Card> cb) {
            cbc = cb;
            serialize(getGame(), 3);
            IPlayerWithCardsAndStock.super.askWhichCardToBuy(cb);
        }

        @Override
        public void askForTargetPlayer(String reason, Consumer1<IPlayer> cb) {
            this.reason = reason;
            cba = cb;
            serialize(getGame(), 4);
            IPlayerWithCardsAndStock.super.askForTargetPlayer(reason, cb);
        }

        @Override
        public void askForTargetPlace(String reason, Consumer1<Place> cb) {
            this.reason = reason;
            cbpl = cb;
            serialize(getGame(), 5);
            IPlayerWithCardsAndStock.super.askForTargetPlace(reason, cb);
        }

        @Override
        public void askWhetherToBuyProperty(Consumer1<Boolean> cb) {
            cbb = cb;
            serialize(getGame(), 6);
            Properties.IPlayerWithProperties.super.askWhetherToBuyProperty(cb);
        }

        @Override
        public void askWhetherToUpgradeProperty(Consumer1<Boolean> cb) {
            cbb = cb;
            serialize(getGame(), 7);
            Properties.IPlayerWithProperties.super.askWhetherToUpgradeProperty(cb);
        }

        @Override
        public void askWhichPropertyToMortgage(Consumer1<Property> cb) {
            cbpr = cb;
            serialize(getGame(), 8);
            Properties.IPlayerWithProperties.super.askWhichPropertyToMortgage(cb);
        }

        @Override
        protected void startStep() {
            serialize(getGame(), 9);
            super.startStep();
        }
        
        private void resume(int i) {
            switch (i) {
                case 1:
                    super.startTurn(cb);
                    break;
                case 2:
                    super.askHowMuchToDepositOrWithdraw(cbi);
                    break;
                case 3:
                    IPlayerWithCardsAndStock.super.askWhichCardToBuy(cbc);
                    break;
                case 4:
                    IPlayerWithCardsAndStock.super.askForTargetPlayer(reason, cba);
                    break;
                case 5:
                    IPlayerWithCardsAndStock.super.askForTargetPlace(reason, cbpl);
                    break;
                case 6:
                    Properties.IPlayerWithProperties.super.askWhetherToBuyProperty(cbb);
                    break;
                case 7:
                    Properties.IPlayerWithProperties.super.askWhetherToUpgradeProperty(cbb);
                    break;
                case 8:
                    Properties.IPlayerWithProperties.super.askWhichPropertyToMortgage(cbpr);
                    break;
                case 9:
                    super.startStep();
                    break;
            }
        }
    }
    
    public GameSerializationTest() throws Exception {
        Class.forName("monopoly.GameMapReader");
        Place.loadAll();
        Card.loadAll();
        Class.forName("monopoly.Shareholding");
        Class.forName("monopoly.stock.StockMarket");
        Class.forName("monopoly.Properties");
        Class.forName("monopoly.Cards");
        
        GameMap map = GameMap.readMap(GameTest.class.getResourceAsStream("/card_rich.map"));

        game.putConfig("original", true);
        game.setMap(map);
        List<IPlayer> players = new ArrayList<>();
        players.add(new Player("player A"));
        players.add(new Player("player B"));
        players.add(new Player("player C"));
        players.add(new Player("player D"));
        game.setPlayers(players);
    }
    
    private static void serialize(Game game, int i) {
        try {
            if (game.getConfig("original")) {
                File tmp = File.createTempFile("game", "tmp");
                FileOutputStream fos = new FileOutputStream(tmp);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                game.writeData(oos);
                oos.close();
                fos.close();
                FileInputStream fis = new FileInputStream(tmp);
                ObjectInputStream ois = new ObjectInputStream(fis);
                Game newGame = Game.readData(ois);
                newGame.putConfig("original", false);
                ((Player)newGame.getCurrentPlayer()).resume(i);
                ois.close();
                fis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
    
    @Test
    public void test() {
        game.start();
    }
}
