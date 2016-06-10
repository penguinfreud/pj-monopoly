package monopoly.gui;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import monopoly.*;
import monopoly.card.ReverseCard;
import monopoly.card.TaxCard;
import monopoly.gui.dialogs.BankDialog;
import monopoly.gui.dialogs.ChoiceDialog;
import monopoly.gui.dialogs.YesOrNoDialog;
import monopoly.place.Place;
import monopoly.util.Consumer0;

import java.util.function.Consumer;
import java.util.function.Function;

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
    public void askWhetherToBuyProperty(Consumer<Boolean> cb) {
        Property property = getCurrentPlace().asProperty();
        boolean result = new YesOrNoDialog(controller,
                controller.getText("buy_property"),
                controller.format("ask_whether_to_buy_property",
                        property.getName(), property.getPurchasePrice(), getCash()))
                .showAndWait().orElse(false);
        cb.accept(result);
    }

    @Override
    public void askWhetherToUpgradeProperty(Consumer<Boolean> cb) {
        Property property = getCurrentPlace().asProperty();
        boolean result = new YesOrNoDialog(controller,
                controller.getText("upgrade_property"),
                controller.format("ask_whether_to_upgrade_property",
                        property.getName(), property.getUpgradePrice(), getCash()))
                .showAndWait().orElse(false);
        cb.accept(result);
    }

    @Override
    public void askHowMuchToDepositOrWithdraw(Consumer<Double> cb) {
        cb.accept(new BankDialog(controller,
                controller.getText("bank"),
                controller.format("ask_how_much_to_deposit_or_withdraw", getCash(), getDeposit()))
                .showAndWait().orElse(0.0));
    }

    @Override
    public void askWhichPropertyToMortgage(Consumer<Property> cb) {
        cb.accept(new ChoiceDialog<>(controller,
                controller.getText("mortgage_property"),
                controller.getText("ask_which_property_to_mortgage"),
                Properties.get(this).getProperties(),
                property -> new Text(controller.format("property_and_mortgage_price",
                        property.toString(getGame()),
                        property.getMortgagePrice())),
                false)
                .showAndWait().get());
    }

    @Override
    public void askWhichCardToBuy(Consumer<Card> cb) {
        cb.accept(new ChoiceDialog<>(controller,
                controller.getText("buy_card"),
                controller.format("ask_which_card_to_buy", Cards.get(this).getCoupons()),
                Cards.getAvailableCards(getGame()),
                card -> new Text(controller.format("card_and_price",
                        card.toString(getGame()),
                        card.getPrice(getGame()))),
                true)
                .showAndWait().orElse(null));
    }

    @Override
    public void askForTargetPlayer(Card card, Consumer<IPlayer> cb) {
        String question;
        if (card instanceof ReverseCard) {
            question = controller.getText("ask_whom_to_reverse");
        } else if (card instanceof TaxCard) {
            question = controller.getText("ask_whom_to_tax");
        } else {
            question = "";
        }
        cb.accept(new ChoiceDialog<>(controller,
                controller.getText("using") + card.toString(getGame()),
                question,
                getGame().getPlayers(),
                player -> new HBox(
                        new ImageView(GUIPlayerInfo.get(player).getIcon(48)),
                        new Text(player.getName())),
                true)
                .showAndWait().orElse(null));
    }
    
    public void useCard() {
        if (getGame().getState() == Game.State.TURN_STARTING &&
                Cards.get(this).getCardsCount() > 0) {
            Card card = new ChoiceDialog<>(controller,
                    controller.getText("use_card"),
                    controller.getText("ask_which_card_to_use"),
                    Cards.get(this).getCards(),
                    _card -> new Text(_card.toString(getGame())),
                    true).showAndWait().orElse(null);
            if (card != null) {
                Cards.get(this).useCard(card, this::useCard);
            }
        }
    }

    @Override
    public void askForTargetPlace(Card card, Consumer<Place> cb) {
        ((GUIGameMap) getGame().getMap()).setOnSelectPlace(cb);
    }
}
