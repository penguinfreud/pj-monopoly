package monopoly.gui.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import monopoly.gui.MainController;

public class PromptDialog<T> extends Dialog<T> {
    protected VBox pane = new VBox();

    public PromptDialog(MainController controller, String title, String prompt) {
        setTitle(controller.getText("monopoly"));
        setOnCloseRequest(e -> close());
        Text text = controller.createText(prompt);

        pane.getChildren().add(text);
        pane.setPadding(new Insets(20));
        getDialogPane().setContent(pane);
    }
}
