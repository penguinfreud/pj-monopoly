package monopoly.gui;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import monopoly.*;
import monopoly.gui.dialogs.BankDialog;
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
                controller.format("ask_whether_to_buy_property",
                        property.getName(), property.getPurchasePrice(), getCash()))
                .showAndWait().orElse(false);
        cb.accept(result);
    }

    @Override
    public void askWhetherToUpgradeProperty(Consumer1<Boolean> cb) {
        Property property = getCurrentPlace().asProperty();
        boolean result = new YesOrNoDialog(controller,
                controller.format("ask_whether_to_upgrade_property",
                        property.getName(), property.getUpgradePrice(), getCash()))
                .showAndWait().orElse(false);
        cb.accept(result);
    }

    @Override
    public void askHowMuchToDepositOrWithdraw(Consumer1<Double> cb) {
        cb.accept(new BankDialog(controller,
                controller.format("ask_how_much_to_deposit_or_withdraw", getCash(), getDeposit()))
                .showAndWait().orElse(0.0));
    }
}
