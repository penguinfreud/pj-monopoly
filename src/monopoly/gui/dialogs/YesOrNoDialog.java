package monopoly.gui.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import monopoly.gui.MainController;

public class YesOrNoDialog extends Dialog<Boolean> {
    public YesOrNoDialog(MainController controller, String prompt) {
        setTitle(controller.getText("monopoly"));
        setOnCloseRequest(e -> close());
        DialogPane dialogPane = getDialogPane();
        Text text = controller.createText(prompt);
        Pane pane = new VBox();
        pane.getChildren().add(text);
        pane.setPadding(new Insets(20));
        dialogPane.setContent(pane);
        LocalButtonTypes buttonTypes = controller.getButtonTypes();
        dialogPane.getButtonTypes().addAll(buttonTypes.YES, buttonTypes.NO);
        setResultConverter(type -> type == ButtonType.YES);
    }
}
