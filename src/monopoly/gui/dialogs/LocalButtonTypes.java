package monopoly.gui.dialogs;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import monopoly.gui.MainController;

public class LocalButtonTypes {
    private MainController controller;

    public final ButtonType YES, NO, OK, CANCEL, DEPOSIT, WITHDRAW;

    public LocalButtonTypes(MainController controller) {
        this.controller = controller;
        YES = new ButtonType(controller.getText("yes"), ButtonBar.ButtonData.YES);
        NO = new ButtonType(controller.getText("no"), ButtonBar.ButtonData.NO);
        OK = new ButtonType(controller.getText("ok"), ButtonBar.ButtonData.OK_DONE);
        CANCEL = new ButtonType(controller.getText("cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        DEPOSIT = new ButtonType(controller.getText("deposit"));
        WITHDRAW = new ButtonType(controller.getText("withdraw"));
    }
}
