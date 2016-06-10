package monopoly.gui;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;

import java.util.Optional;
import java.util.function.Function;

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

    public static <T, U> void bind(Property<U> result, ObservableValue<T> obj, Function<T, ObservableValue<U>> accessor) {
        obj.addListener((observable, oldValue, newValue) -> {
            result.unbind();
            if (newValue != null) {
                result.bind(accessor.apply(newValue));
            }
        });
    }

    public static <T, U> void bindBidirectional(Property<U> result, ObservableValue<T> obj, Function<T, Property<U>> accessor) {
        obj.addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                result.unbindBidirectional(accessor.apply(oldValue));
            }
            if (newValue != null) {
                result.bindBidirectional(accessor.apply(newValue));
            }
        });
    }
}
