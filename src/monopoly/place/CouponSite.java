package monopoly.place;

import monopoly.*;
import monopoly.util.Consumer0;

import java.util.concurrent.ThreadLocalRandom;

public class CouponSite extends Place {
    static {
        Map.registerPlaceReader("CouponSite", (r, sc) -> new CouponSite());
        Game.putDefaultConfig("coupon-award-min", 1);
        Game.putDefaultConfig("coupon-award-max", 10);
    }

    private CouponSite() {
        super("CouponSite");
    }

    @Override
    public void onLanded(Game g, PlaceInterface pi, Consumer0 cb) {
        int min = g.getConfig("coupon-award-min"),
                max = g.getConfig("coupon-award-max");
        pi.addCoupons(g.getCurrentPlayer(), ThreadLocalRandom.current().nextInt(max - min + 1) + min);
        cb.run();
    }
}
