package monopoly.tui;

import monopoly.*;
import monopoly.place.*;
import monopoly.place.Bank;

import java.io.PrintStream;
import java.util.List;

public class TUIPlace extends DelegatePlace {
    static {
        Map.registerPlaceReader("TUIPlace", (r, sc) -> new TUIPlace(sc.nextInt(), sc.nextInt(), r.readPlace(sc)));
    }

    private int x, y;

    public TUIPlace(int x, int y, Place place) {
        super(place);
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    String print(Game g, boolean raw) {
        Place place = getPlace();
        List<AbstractPlayer> players = g.getPlayers();
        if (!raw && this == players.get(0).getCurrentPlace()) {
            return "□ ";
        } else if (!raw && this == players.get(1).getCurrentPlace()) {
            return "■ ";
        } else if (place instanceof Property) {
            Property prop = place.asProperty();
            if (raw || prop.getOwner() == null) {
                return "◎ ";
            } else if (prop.getOwner() == players.get(0)) {
                return "○ ";
            } else {
                return "● ";
            }
        } else if (place instanceof Bank) {
            return "银";
        } else if (place instanceof News) {
            return "新";
        } else if (place instanceof Empty) {
            return "空";
        } else if (place instanceof CouponSite) {
            return "券";
        } else if (place instanceof CardSite) {
            return "卡";
        } else if (place instanceof CardShop) {
            return "店";
        }
        return "  ";
    }

    void printDetail(Game g, PrintStream out) {
        Place place = getPlace();
        if (place instanceof Land) {
            Property prop = place.asProperty();
            out.println(prop.getName());
            out.print(g.getText("place_type"));
            out.println(g.getText("place_land"));
            out.print(g.getText("land_owner"));
            AbstractPlayer owner = prop.getOwner();
            if (owner == null) {
                out.println(g.getText("none"));
            } else {
                out.println(prop.getOwner().getName());
            }
            out.print(g.getText("land_initial_price"));
            out.println(prop.getPrice());
            out.print(g.getText("land_level"));
            out.println(prop.getLevel());
            out.print(g.getText("land_rent"));
            out.println(prop.getRent());
        } else {
            out.print(g.getText("place_type"));
            out.println(place.toString(g));
        }
    }
}
