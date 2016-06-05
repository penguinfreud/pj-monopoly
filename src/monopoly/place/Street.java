package monopoly.place;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Street {
    private final String name;
    private final ObservableList<Land> lands = FXCollections.observableList(new CopyOnWriteArrayList<>());

    public Street(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addLand(Land land) {
        lands.add(land);
    }

    public List<Land> getLands() {
        return new CopyOnWriteArrayList<>(lands);
    }

    public DoubleBinding getExtraRent(Land ref) {
        DoubleBinding binding = Bindings.createDoubleBinding(
                () -> lands.stream()
                        .filter((land) -> land.getOwner() == ref.getOwner())
                        .map(Land::getPrice)
                        .reduce(0.0, (a, b) -> a + b) / 10);
        InvalidationListener listener = e -> binding.invalidate();
        lands.addListener((ListChangeListener<? super Land>) change -> {
            while (change.next()) {
                if (change.wasRemoved()) {
                    for (Land land : change.getRemoved()) {
                        land.ownerProperty().removeListener(listener);
                        land.priceProperty().removeListener(listener);
                    }
                }
                if (change.wasAdded()) {
                    for (Land land : change.getAddedSubList()) {
                        land.ownerProperty().addListener(listener);
                        land.priceProperty().addListener(listener);
                    }
                }
            }
        });
        return binding;
    }
}
