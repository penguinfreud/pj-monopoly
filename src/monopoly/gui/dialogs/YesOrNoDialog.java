package monopoly.gui.dialogs;

import monopoly.gui.MainController;

public class YesOrNoDialog extends PromptDialog<Boolean> {
    public YesOrNoDialog(MainController controller, String title, String prompt) {
        super(controller, title, prompt);

        LocalButtonTypes buttonTypes = controller.getButtonTypes();
        getDialogPane().getButtonTypes().addAll(buttonTypes.YES, buttonTypes.NO);
        setResultConverter(type -> type == buttonTypes.YES);
    }
}
