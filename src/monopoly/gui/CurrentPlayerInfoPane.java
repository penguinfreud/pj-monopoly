package monopoly.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import monopoly.Cards;
import monopoly.IPlayer;
import monopoly.Properties;

import java.util.Hashtable;
import java.util.Map;

public class CurrentPlayerInfoPane extends VBox {
    private static final Map<MainController, CurrentPlayerInfoPane> parasites = new Hashtable<>();

    public static CurrentPlayerInfoPane get(MainController controller) {
        CurrentPlayerInfoPane pane = parasites.get(controller);
        if (pane == null) {
            pane = new CurrentPlayerInfoPane(controller);
            parasites.put(controller, pane);
        }
        return pane;
    }

    public CurrentPlayerInfoPane(MainController controller) {
        Text nameText = new Text();
        Text cashText = new Text();
        Text depositText = new Text();
        Text couponsText = new Text();
        Text cardsText = new Text();
        Text propertiesText = new Text();
        ImageView iconView = new ImageView();
        getChildren().addAll(
                iconView,
                new HBox(new Text(controller.getText("name_colon")), nameText),
                new HBox(new Text(controller.getText("cash_colon")), cashText),
                new HBox(new Text(controller.getText("deposit_colon")), depositText),
                new HBox(new Text(controller.getText("coupons_colon")), couponsText),
                new HBox(new Text(controller.getText("cards_colon")), cardsText),
                new HBox(new Text(controller.getText("properties_colon")), propertiesText));
        ObjectBinding<IPlayer> currentPlayer = controller.getGame().currentPlayer();
        Util.bind(nameText.textProperty(), currentPlayer, IPlayer::nameProperty);
        Util.bind(cashText.textProperty(), currentPlayer,
                player -> player.cashProperty().asString("%.2f"));
        Util.bind(depositText.textProperty(), currentPlayer,
                player -> player.depositProperty().asString("%.2f"));
        Util.bind(couponsText.textProperty(), currentPlayer,
                player -> Cards.get(player).couponsProperty().asString());
        Util.bind(cardsText.textProperty(), currentPlayer,
                player -> Bindings.size(Cards.get(player).getCards()).asString());
        Util.bind(propertiesText.textProperty(), currentPlayer,
                player -> Bindings.size(Properties.get(player).getProperties()).asString());
        Util.bind(iconView.imageProperty(), currentPlayer,
                player -> GUIPlayerInfo.get(player).icon(144));
    }


}
