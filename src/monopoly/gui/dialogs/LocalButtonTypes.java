package monopoly.gui.dialogs;

import javafx.scene.control.ButtonType;
import monopoly.gui.MainController;

public class LocalButtonTypes {
    private MainController controller;

    public final ButtonType YES, NO, CANCEL, DEPOSIT, WITHDRAW;

    public LocalButtonTypes(MainController controller) {
        this.controller = controller;
        YES = new ButtonType(controller.getText("yes"));
        NO = new ButtonType(controller.getText("no"));
        CANCEL = new ButtonType(controller.getText("cancel"));
        DEPOSIT = new ButtonType(controller.getText("deposit"));
        WITHDRAW = new ButtonType(controller.getText("withdraw"));
    }
}
