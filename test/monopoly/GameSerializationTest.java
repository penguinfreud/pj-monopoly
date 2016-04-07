package monopoly;

import monopoly.util.Consumer0;
import monopoly.util.Consumer1;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameSerializationTest {
    private Game game = new Game();
    
    private List<Game> games = new CopyOnWriteArrayList<>();
    
    private static class Player extends AIPlayer {
        private Consumer0 cb;
        private Consumer1<Integer> cbi;
        private Consumer1<Boolean> cbb;
        private Consumer1<Property> cbpr;
        private Consumer1<Place> cbpl;
        private Consumer1<Card> cbc;
        private Consumer1<AbstractPlayer> cba;
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
            super.askWhichCardToBuy(cb);
        }

        @Override
        public void askForTargetPlayer(String reason, Consumer1<AbstractPlayer> cb) {
            this.reason = reason;
            cba = cb;
            serialize(getGame(), 4);
            super.askForTargetPlayer(reason, cb);
        }

        @Override
        public void askForTargetPlace(String reason, Consumer1<Place> cb) {
            this.reason = reason;
            cbpl = cb;
            serialize(getGame(), 5);
            super.askForTargetPlace(reason, cb);
        }

        @Override
        public void askWhetherToBuyProperty(Consumer1<Boolean> cb) {
            cbb = cb;
            serialize(getGame(), 6);
            super.askWhetherToBuyProperty(cb);
        }

        @Override
        public void askWhetherToUpgradeProperty(Consumer1<Boolean> cb) {
            cbb = cb;
            serialize(getGame(), 7);
            super.askWhetherToUpgradeProperty(cb);
        }

        @Override
        public void askWhichPropertyToMortgage(Consumer1<Property> cb) {
            cbpr = cb;
            serialize(getGame(), 8);
            super.askWhichPropertyToMortgage(cb);
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
                    super.askWhichCardToBuy(cbc);
                    break;
                case 4:
                    super.askForTargetPlayer(reason, cba);
                    break;
                case 5:
                    super.askForTargetPlace(reason, cbpl);
                    break;
                case 6:
                    super.askWhetherToBuyProperty(cbb);
                    break;
                case 7:
                    super.askWhetherToUpgradeProperty(cbb);
                    break;
                case 8:
                    super.askWhichPropertyToMortgage(cbpr);
                    break;
                case 9:
                    super.startStep();
                    break;
            }
        }
    }
    
    public GameSerializationTest() throws Exception {
        Class.forName("monopoly.GameMapReader");
        Class.forName("monopoly.place.Empty");
        Class.forName("monopoly.place.Land");
        Class.forName("monopoly.place.Bank");
        Class.forName("monopoly.place.News");
        Class.forName("monopoly.place.CouponSite");
        Class.forName("monopoly.place.CardSite");
        Class.forName("monopoly.place.CardShop");
        Class.forName("monopoly.place.Trap");
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
        Class.forName("monopoly.Shareholding");
        Class.forName("monopoly.Properties");
        Class.forName("monopoly.Cards");
        
        GameMap map = GameMap.readMap(GameTest.class.getResourceAsStream("/card_rich.map"));

        game.putConfig("original", true);
        game.setMap(map);
        List<AbstractPlayer> players = new ArrayList<>();
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
