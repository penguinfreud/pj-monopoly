package monopoly.place;

import monopoly.Game;
import monopoly.Cards;
import monopoly.util.Consumer0;

import java.util.concurrent.ThreadLocalRandom;

public class CouponSite extends Place {
    static {
        GameMap.registerPlaceReader("CouponSite", (r, sc) -> new CouponSite());
        Game.putDefaultConfig("coupon-award-min", 1);
        Game.putDefaultConfig("coupon-award-max", 10);
    }

    private CouponSite() {
        super("CouponSite");
    }

    @Override
    public void init(Game g) {
        Cards.init(g);
    }

    @Override
    public void arriveAt(Game g, Consumer0 cb) {
        int min = g.getConfig("coupon-award-min"),
                max = g.getConfig("coupon-award-max");
        Cards.get(g.getCurrentPlayer()).addCoupons(ThreadLocalRandom.current().nextInt(max - min + 1) + min);
        cb.run();
    }
}
