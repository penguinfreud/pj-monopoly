package monopoly.place;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Street implements Serializable {
    private String name;
    private List<Land> lands = new CopyOnWriteArrayList<>();

    public Street(String name) {
        this.name = name;
    }

    public void addLand(Land land) {
        lands.add(land);
    }

    public List<Land> getLands() {
        return new CopyOnWriteArrayList<>(lands);
    }

    public int getExtraRent() {
        int rent = 0;
        for (Land land: lands) {
            rent += land.getPrice();
        }
        return rent / 10;
    }
}
