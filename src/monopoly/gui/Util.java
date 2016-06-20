package monopoly.gui;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;

import java.util.Optional;

public class Util {
    public static <T> Optional<T> getListSelection(ListView<T> listView) {
        ObservableList<T> sel = listView.getSelectionModel().getSelectedItems();
        if (sel.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(sel.get(0));
        }
    }

    public static void alertError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.setResizable(true);
        alert.showAndWait();
    }

}
