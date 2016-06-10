package monopoly.gui.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import monopoly.gui.MainController;

public class BankDialog extends Dialog<Double> {
    public BankDialog(MainController controller, String prompt) {
        setTitle(controller.getText("monopoly"));
        setOnCloseRequest(e -> close());

        DialogPane dialogPane = getDialogPane();
        Text text = controller.createText(prompt);
        TextField amountField = new TextField();
        Pane pane = new VBox();
        pane.getChildren().addAll(text, amountField);
        pane.setPadding(new Insets(20));
        dialogPane.setContent(pane);

        LocalButtonTypes buttonTypes = controller.getButtonTypes();
        dialogPane.getButtonTypes().addAll(buttonTypes.DEPOSIT, buttonTypes.WITHDRAW, buttonTypes.CANCEL);
        setResultConverter(type -> {
            if (type == buttonTypes.CANCEL)
                return 0.0;
            try {
                double amount = Double.parseDouble(amountField.getText());
                return type == buttonTypes.DEPOSIT ? amount : -amount;
            } catch (NumberFormatException e) {
                return 0.0;
            }
        });
    }
}
