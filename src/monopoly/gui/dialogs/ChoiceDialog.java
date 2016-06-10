package monopoly.gui.dialogs;

import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import monopoly.gui.MainController;

import java.util.List;
import java.util.function.Function;

public class ChoiceDialog<T> extends PromptDialog<T> {
    private T selectedItem;

    public ChoiceDialog(MainController controller,
                        String title,
                        String prompt,
                        List<T> items,
                        Function<T, Node> toGraphic,
                        boolean nullable) {
        super(controller, title, prompt);

        VBox itemsPane = new VBox();
        ToggleGroup toggleGroup = new ToggleGroup();

        boolean first = true;
        for (T t : items) {
            RadioButton radioButton = new RadioButton();
            radioButton.setGraphic(toGraphic.apply(t));
            radioButton.setToggleGroup(toggleGroup);
            itemsPane.getChildren().add(radioButton);
            radioButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    selectedItem = t;
                }
            });
            if (first) {
                radioButton.setSelected(true);
                first = false;
            }
        }

        pane.getChildren().add(itemsPane);

        LocalButtonTypes buttonTypes = controller.getButtonTypes();
        getDialogPane().getButtonTypes().add(buttonTypes.OK);
        if (nullable) {
            getDialogPane().getButtonTypes().add(buttonTypes.CANCEL);
        }

        setResultConverter(type -> {
            if (type == buttonTypes.CANCEL)
                return null;
            return selectedItem;
        });
    }
}
