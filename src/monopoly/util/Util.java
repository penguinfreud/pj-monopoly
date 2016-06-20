package monopoly.util;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.function.Function;

public class Util {
    private static final DecimalFormat df = new DecimalFormat("#,##0.00");

    public static final String formatNumber(double val) {
        if (Double.isNaN(val)) {
            return "n/a";
        } else {
            return df.format(val);
        }
    }

    public static String getText(ResourceBundle messages, String key) {
        try {
            return new String(messages.getString(key).getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        } catch (MissingResourceException e) {
            //logger.log(Level.INFO, "Unknown key: " + key);
            return key;
        }

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

    public static <T> DoubleBinding sum(ObservableList<T> list, Function<T, DoubleBinding> getNumber) {
        DoubleBinding result = Bindings.createDoubleBinding(() -> list.stream().map(getNumber)
                .map(DoubleBinding::get)
                .reduce(0.0, (a, b) -> a + b), list);
        InvalidationListener listener = e -> result.invalidate();
        list.addListener((ListChangeListener<? super T>)  change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (T t: change.getAddedSubList()) {
                        getNumber.apply(t).addListener(listener);
                    }
                } else if (change.wasRemoved()) {
                    for (T t: change.getRemoved()) {
                        getNumber.apply(t).removeListener(listener);
                    }
                }
            }
        });
        for (T t: list) {
            getNumber.apply(t).addListener(listener);
        }
        return result;
    }
}
