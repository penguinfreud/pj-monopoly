package monopoly.place;

import monopoly.AbstractPlayer;
import monopoly.Game;
import monopoly.Map;
import monopoly.Place;
import monopoly.async.Callback;

import java.util.concurrent.ThreadLocalRandom;

public class CouponSite extends Place {
    static {
        Map.registerPlaceReader("CouponSite", (r, sc) -> new CouponSite());
    }

    private CouponSite() {
        super("CouponSite");
    }

    @Override
    public void onLanded(Game g, AbstractPlayer.PlaceInterface pi, Callback<Object> cb) {
        int min = (Integer) g.getConfig("coupon-award-min"),
                max = (Integer) g.getConfig("coupon-award-max");
        pi.addCoupons(g.getCurrentPlayer(), g, ThreadLocalRandom.current().nextInt(max - min + 1) + min);
    }
}
