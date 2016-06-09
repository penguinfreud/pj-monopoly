package monopoly.gui.dialogs;

import javafx.scene.control.ButtonType;
import monopoly.gui.MainController;

public class LocalButtonTypes {
    private MainController controller;

    public final ButtonType YES, NO;

    public LocalButtonTypes(MainController controller) {
        this.controller = controller;
        YES = new ButtonType(controller.getText("yes"));
        NO = new ButtonType(controller.getText("no"));
    }
}
