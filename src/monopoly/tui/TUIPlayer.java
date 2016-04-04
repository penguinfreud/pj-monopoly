package monopoly.tui;

import monopoly.*;
import monopoly.card.Card;
import monopoly.util.Consumer0;
import monopoly.util.Consumer1;
import monopoly.util.Function1;

import java.util.ArrayList;
import java.util.List;

public class TUIPlayer extends AbstractPlayer {
    public TUIPlayer() {}

    public TUIPlayer(String name) {
        setName(name);
    }

    private boolean yesOrNo(String question) {
        Game g = getGame();
        while (true) {
            System.out.print(question);
            String answer = ((TUIGame) g).getScanner().nextLine().toLowerCase();
            if (answer.equals("y") || answer.equals("yes")) {
                return true;
            } else if (answer.equals("n") || answer.equals("no")) {
                return false;
            }
            System.out.println(g.getText("input_error"));
        }
    }

    private int getInt(String question, int min, int max, int noop) {
        TUIGame g = (TUIGame) getGame();
        while (true) {
            System.out.print(question);
            String str = g.getScanner().nextLine();
            if (str.toLowerCase().equals("q")) {
                return noop;
            }
            try {
                int x = Integer.parseInt(str);
                if (x >= min && x <= max) {
                    return x;
                }
            } catch (NumberFormatException e) {}
            System.out.println(g.getText("input_error"));
        }
    }

    private <T extends GameObject>T choose(String question, List<T> options, boolean nullable, Function1<T, String> stringifier) {
        TUIGame g = (TUIGame) getGame();
        while (true) {
            System.out.println(question);
            int l = options.size();
            for (int i = 0; i<l; i++) {
                T item = options.get(i);
                String option = stringifier == null? item.toString(g): stringifier.run(item);
                System.out.println("[" + (i + 1) + "] " + option);
            }
            if (nullable) {
                System.out.println("[" + (l + 1) + "] " + g.getText("return"));
            }
            String strChoice = g.getScanner().nextLine();
            try {
                int choice = Integer.parseInt(strChoice);
                if (choice >= 1 && choice <= l) {
                    return options.get(choice - 1);
                } else if (nullable && choice == l + 1) {
                    return null;
                }
            } catch (NumberFormatException e) {}
            System.out.println(g.getText("input_error"));
        }
    }

    private <T extends GameObject>T choose(String question, List<T> options, boolean nullable) {
        return choose(question, options, nullable, null);
    }

    private int chooseInt(Game g, List<String> options, String prompt) {
        while (true) {
            int l = options.size();
            for (int i = 0; i<l; i++) {
                System.out.println("[" + (i + 1) + "] " + options.get(i));
            }
            System.out.print(prompt);
            String strChoice = ((TUIGame) g).getScanner().nextLine();
            try {
                int choice = Integer.parseInt(strChoice);
                if (choice >= 1 && choice <= l) {
                    return choice - 1;
                }
            } catch (NumberFormatException e) {}
            System.out.println(g.getText("input_error"));
        }
    }

    @Override
    protected void askWhetherToBuyProperty(Consumer1<Boolean> cb) {
        Property property = getCurrentPlace().asProperty();
        String question = getGame().format("ask_whether_to_buy_property", property.getName(), property.getPurchasePrice(), getCash());
        cb.run(yesOrNo(question));
    }

    @Override
    protected void askWhetherToUpgradeProperty(Consumer1<Boolean> cb) {
        Property property = getCurrentPlace().asProperty();
        String question = getGame().format("ask_whether_to_upgrade_property", property.getName(), property.getUpgradePrice(), getCash());
        cb.run(yesOrNo(question));
    }

    @Override
    protected void askWhichPropertyToMortgage(Consumer1<Property> cb) {
        String question = getGame().getText("ask_which_property_to_mortgage");
        cb.run(choose(question, getProperties(), false));
    }

    @Override
    protected void askWhichCardToBuy(Consumer1<Card> cb) {
        String question = getGame().format("ask_which_card_to_buy", getCoupons());
        cb.run(choose(question, Card.getCards(), true));
    }

    private void viewMap(Game g, boolean raw) {
        ((TUIMap) g.getMap()).print(g, System.out, raw);
    }

    private Card _askWhichCardToUse() {
        List<Card> cards = getCards();
        if (cards.size() == 0) {
            System.out.println(getGame().getText("you_have_no_card"));
            return null;
        } else {
            String question = getGame().getText("ask_which_card_to_use");
            return choose(question, cards, true);
        }
    }

    private void checkAlert(Game g) {
        Place place = getCurrentPlace();
        boolean hasRoadblock = false;
        for (int i = 0; i<10; i++) {
            place = place.getNext();
            if (place.hasRoadblock()) {
                hasRoadblock = true;
                System.out.println(g.format("has_roadblock", i + 1));
            }
        }
        if (!hasRoadblock) {
            System.out.println(g.getText("has_no_roadblock"));
        }
    }

    private void viewPlace() {
        Game g = getGame();
        while (true) {
            int steps = getInt(g.getText("ask_which_place_to_view"), 0, 10, -1);
            if (steps == -1) {
                break;
            }
            Place place = getCurrentPlace();
            for (int i = 0; i<steps; i++) {
                place = isReversed()? place.getPrev(): place.getNext();
            }
            ((TUIPlace)place).printDetail(g, System.out);
        }
    }

    private void viewPlayerInfo() {
        Game g = getGame();
        System.out.println(g.getText("player_info_table_head"));
        for (AbstractPlayer player: g.getPlayers()) {
            System.out.println(g.format("player_info_table_row",
                    player.getCash(),
                    player.getDeposit(),
                    player.getProperties().size(),
                    player.getCoupons(),
                    player.getTotalPossessions()));
        }
    }

    private void tradeStock() {

    }

    @Override
    protected void startTurn(Consumer0 cb) {
        Game g = getGame();
        String direction = g.getText(isReversed()? "anticlockwise": "clockwise");
        System.out.println(g.format("game_info", g.getDate(), getName(), direction));

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
                    Card card = _askWhichCardToUse();
                    if (card != null) {
                        useCard(card, () -> startTurn(cb));
                        break loop;
                    } else {
                        break;
                    }
                case 3:
                    checkAlert(g);
                    break;
                case 4:
                    viewPlace();
                    break;
                case 5:
                    viewPlayerInfo();
                    break;
                case 6:
                    cb.run();
                    break loop;
                case 7:
                    giveUp();
                    cb.run();
                    break loop;
                case 8:
                    tradeStock();
                    break;
            }
        }
    }

    @Override
    protected void askHowMuchToDepositOrWithdraw(Consumer1<Integer> cb) {
        Game g = getGame();
        int maxTransfer = g.getConfig("bank-max-transfer");
        String question = g.format("ask_how_much_to_deposit_or_withdraw", getCash(), getDeposit());
        cb.run(getInt(question, -maxTransfer, maxTransfer, 0));
    }

    @Override
    public void askForPlayer(String reason, Consumer1<AbstractPlayer> cb) {
        Game g = getGame();
        if (reason.equals("ReverseCard")) {
            String question = g.getText("ask_whom_to_reverse");
            List<AbstractPlayer> players = g.getPlayers();
            AbstractPlayer player = choose(question, players, true);
            cb.run(player);
        } else {
            cb.run(null);
        }
    }

    @Override
    public void askForPlace(String reason, Consumer1<Place> cb) {
        Game g = getGame();
        if (reason.equals("ControlledDice")) {
            int steps = getInt(g.format("ask_where_to_go", getCurrentPlace().getName()), 1, 6, 0);
            if (steps == 0) {
                cb.run(null);
            }
            Place place = getCurrentPlace();
            for (int i = 0; i<steps; i++) {
                place = isReversed()? place.getPrev(): place.getNext();
            }
            cb.run(place);
        } else if (reason.equals("Roadblock")) {
            int steps = getInt(g.format("ask_where_to_set_roadblock", getCurrentPlace().getName()), -8, 8, -9);
            if (steps == -9) {
                cb.run(null);
            }
            Place place = getCurrentPlace();
            if (steps > 0) {
                for (int i = 0; i < steps; i++) {
                    place = isReversed()? place.getPrev(): place.getNext();
                }
            } else if (steps < 0) {
                for (int i = 0; i < steps; i++) {
                    place = isReversed()? place.getNext(): place.getPrev();
                }
            }
            cb.run(place);
        } else {
            cb.run(null);
        }
    }
}
