package monopoly.gui.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import monopoly.gui.MainController;

public class StockTradeDialog extends Dialog<Integer> {
    public StockTradeDialog(MainController controller) {
        setTitle(controller.getText("monopoly"));
        setOnCloseRequest(e -> close());

        TextField amountField = new TextField();

        HBox pane = new HBox(new Text(controller.getText("amount_colon")),
                amountField);
        pane.setPadding(new Insets(20));
        getDialogPane().setContent(pane);
        LocalButtonTypes buttonTypes = controller.getButtonTypes();
        getDialogPane().getButtonTypes().addAll(buttonTypes.BUY, buttonTypes.SELL, buttonTypes.CANCEL);

        setResultConverter(type -> {
            int amount;
            try {
                amount = Integer.parseInt(amountField.getText());
            } catch (NumberFormatException e) {
                return 0;
            }

            if (type == buttonTypes.BUY) {
                return amount;
            } else if (type == buttonTypes.SELL) {
                return -amount;
            } else {
                return 0;
            }
        });
    }
}
