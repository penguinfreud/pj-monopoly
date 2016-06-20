package monopoly.gui;

import javafx.scene.control.Alert;

public class Util {
    public static void alertError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.setResizable(true);
        alert.showAndWait();
    }

}
