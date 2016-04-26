package monopoly.place;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Street implements Serializable {
    private final String name;
    private final List<Land> lands = new CopyOnWriteArrayList<>();

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

    public double getExtraRent(Land ref) {
        return lands.stream().filter((land) -> land.getOwner() == ref.getOwner())
                .map(Land::getPrice).reduce(0.0, (a, b) -> a + b) / 10;
    }
}
