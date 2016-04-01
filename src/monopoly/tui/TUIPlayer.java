package monopoly.tui;

import monopoly.*;
import monopoly.async.Callback;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class TUIPlayer extends AbstractPlayer {
    public TUIPlayer() {}

    public TUIPlayer(String name) {
        setName(name);
    }

    private String format(Game g, String key, Object ... args) {
        return MessageFormat.format(g.getText(key), args);
    }

    private boolean yesOrNo(Game g, String question) {
        while (true) {
            System.out.print(question);
            String answer = ((TUIGame) g).getScanner().nextLine().toLowerCase();
            if (answer.equals("y") || answer.equals("yes")) {
                return true;
            } else if (answer.equals("n") || answer.equals("no")) {
                return false;
            }
            System.out.println(answer);
            System.out.println(g.getText("input_error"));
        }
    }

    private <T>T choose(Game g, String question, List<T> options, boolean nullable) {
        while (true) {
            System.out.println(question);
            int l = options.size();
            for (int i = 0; i<l; i++) {
                System.out.println("[" + (i + 1) + "] " + options.get(i));
            }
            if (nullable) {
                System.out.println(g.getText("none"));
            }
            String strChoice = ((TUIGame) g).getScanner().nextLine();
            int choice = Integer.parseInt(strChoice);
            if (choice >= 1 && choice <= l) {
                return options.get(choice - 1);
            } else if (nullable && choice == l + 1) {
                return null;
            }
            System.out.println(g.getText("input_error"));
        }
    }

    private int chooseInt(Game g, List<String> options, String prompt) {
        while (true) {
            int l = options.size();
            for (int i = 0; i<l; i++) {
                System.out.println("[" + (i + 1) + "] " + options.get(i));
            }
            String strChoice = ((TUIGame) g).getScanner().nextLine();
            int choice = Integer.parseInt(strChoice);
            if (choice >= 1 && choice <= l) {
                return choice - 1;
            }
            System.out.println(g.getText("input_error"));
        }
    }

    @Override
    protected void askWhetherToBuyProperty(Game g, Callback<Boolean> cb) {
        AbstractPlayer player = g.getCurrentPlayer();
        Property property = player.getCurrentPlace().asProperty();
        String question = format(g, "ask_whether_to_buy_property", property.getName(), property.getPurchasePrice(), player.getCash());
        cb.run(yesOrNo(g, question));
    }

    @Override
    protected void askWhetherToUpgradeProperty(Game g, Callback<Boolean> cb) {
        AbstractPlayer player = g.getCurrentPlayer();
        Property property = player.getCurrentPlace().asProperty();
        String question = format(g, "ask_whether_to_upgrade_property", property.getName(), property.getUpgradePrice(), player.getCash());
        cb.run(yesOrNo(g, question));
    }

    @Override
    protected void askWhichPropertyToMortgage(Game g, Callback<Property> cb) {
        String question = format(g, "ask_which_property_to_mortgage");
        cb.run(choose(g, question, g.getCurrentPlayer().getProperties(), false));
    }

    private void viewMap(Game g, boolean raw) {

    }

    private Card _askWhichCardToUse(Game g) {
        return null;
    }

    private void checkAlert(Game g) {

    }

    private void viewPlace(Game g) {

    }

    private void viewPlayerInfo(Game g) {

    }

    private void tradeStock(Game g) {

    }

    @Override
    protected void askWhichCardToUse(Game g, Callback<Card> cb) {
        String direction = g.getText(isReversed()? "anticlockwise": "clockwise");
        System.out.println(format(g, "game_info", g.getDate(), getName(), direction));

        List<String> gameMenuItems = new ArrayList<>();
        gameMenuItems.add(g.getText("menu_view_map"));
        gameMenuItems.add(g.getText("menu_view_raw_map"));
        gameMenuItems.add(g.getText("menu_use_card"));
        gameMenuItems.add(g.getText("menu_check_alert"));
        gameMenuItems.add(g.getText("menu_view_place"));
        gameMenuItems.add(g.getText("menu_player_info"));
        gameMenuItems.add(g.getText("menu_roll_the_dice"));
        gameMenuItems.add(g.getText("menu_give_up"));
        gameMenuItems.add(g.getText("menu_stock"));

        loop: while (true) {
            switch (chooseInt(g, gameMenuItems, g.getText("please_choose"))) {
                case 0:
                    viewMap(g, false);
                    break;
                case 1:
                    viewMap(g, true);
                    break;
                case 2:
                    cb.run(_askWhichCardToUse(g));
                    break loop;
                case 3:
                    checkAlert(g);
                    break;
                case 4:
                    viewPlace(g);
                    break;
                case 5:
                    viewPlayerInfo(g);
                    break;
                case 6:
                    cb.run(null);
                    break loop;
                case 7:
                    giveUp(g);
                    cb.run(null);
                    break loop;
                case 8:
                    tradeStock(g);
                    break;
            }
        }
    }

    @Override
    protected void askHowMuchToDepositOrWithdraw(Game g, Callback<Integer> cb) {

    }

    @Override
    public void askWhomToReverse(Game g, Callback<AbstractPlayer> cb) {

    }

    @Override
    public void askWhereToGo(Game g, Callback<Place> cb) {

    }

    @Override
    public void askWhereToSetRoadblock(Game g, Callback<Place> cb) {

    }
}
