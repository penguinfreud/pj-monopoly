package monopoly.gui.dialogs;

import javafx.scene.control.TextField;
import monopoly.gui.MainController;

public class BankDialog extends PromptDialog<Double> {
    public BankDialog(MainController controller, String title, String prompt) {
        super(controller, title, prompt);

        TextField amountField = new TextField();
        pane.getChildren().addAll(amountField);

        LocalButtonTypes buttonTypes = controller.getButtonTypes();
        getDialogPane().getButtonTypes().addAll(
                buttonTypes.DEPOSIT, buttonTypes.WITHDRAW, buttonTypes.CANCEL);

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
