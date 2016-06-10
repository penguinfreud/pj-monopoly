package monopoly.gui;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import monopoly.*;
import monopoly.gui.dialogs.BankDialog;
import monopoly.gui.dialogs.ChoiceDialog;
import monopoly.gui.dialogs.YesOrNoDialog;
import monopoly.util.Consumer0;
import monopoly.util.Consumer1;

public class GUIPlayer extends BasePlayer implements Properties.IPlayerWithProperties, IPlayerWithCardsAndStock {
    private MainController controller;

    public GUIPlayer(Game g, MainController controller) {
        super(g);
        this.controller = controller;
    }

    @Override
    public void startTurn(Consumer0 cb) {
        DiceView diceView = DiceView.get(controller);
        EventHandler<MouseEvent> listener = e -> {
            diceView.setOnMouseClicked(null);
            cb.accept();
        };
        diceView.setOnMouseClicked(listener);
    }

    @Override
    public void askWhetherToBuyProperty(Consumer1<Boolean> cb) {
        Property property = getCurrentPlace().asProperty();
        boolean result = new YesOrNoDialog(controller,
                controller.getText("buy_property"),
                controller.format("ask_whether_to_buy_property",
                        property.getName(), property.getPurchasePrice(), getCash()))
                .showAndWait().orElse(false);
        cb.accept(result);
    }

    @Override
    public void askWhetherToUpgradeProperty(Consumer1<Boolean> cb) {
        Property property = getCurrentPlace().asProperty();
        boolean result = new YesOrNoDialog(controller,
                controller.getText("upgrade_property"),
                controller.format("ask_whether_to_upgrade_property",
                        property.getName(), property.getUpgradePrice(), getCash()))
                .showAndWait().orElse(false);
        cb.accept(result);
    }

    @Override
    public void askHowMuchToDepositOrWithdraw(Consumer1<Double> cb) {
        cb.accept(new BankDialog(controller,
                controller.getText("bank"),
                controller.format("ask_how_much_to_deposit_or_withdraw", getCash(), getDeposit()))
                .showAndWait().orElse(0.0));
    }

    @Override
    public void askWhichPropertyToMortgage(Consumer1<Property> cb) {
        cb.accept(new ChoiceDialog<>(controller,
                controller.getText("mortgage_property"),
                controller.getText("ask_which_property_to_mortgage"),
                Properties.get(this).getProperties(),
                property -> controller.format("property_and_mortgage_price",
                        property.toString(getGame()),
                        property.getMortgagePrice()),
                false)
                .showAndWait().get());
    }

    @Override
    public void askWhichCardToBuy(Consumer1<Card> cb) {
        cb.accept(new ChoiceDialog<>(controller,
                controller.getText("buy_card"),
                controller.format("ask_which_card_to_buy", Cards.get(this).getCoupons()),
                Cards.getAvailableCards(getGame()),
                card -> controller.format("card_and_price",
                        card.toString(getGame()),
                        card.getPrice(getGame())),
                true)
                .showAndWait().orElse(null));
    }
}
