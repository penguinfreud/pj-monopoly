package monopoly;

import monopoly.card.*;
import monopoly.util.Consumer1;

public abstract class Card implements GameObject {
    private final String name;

    protected Card(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString(Game g) {
        return g.getText("card_" + name.toLowerCase());
    }

    public int getPrice(Game g) {
        return (Integer) g.getConfig(uncamelize(name) + "-price");
    }

    protected abstract void use(Game g, Consumer1<Boolean> cb);

    private String uncamelize(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch >= 'A' && ch <= 'Z') {
                if (i > 0) {
                    sb.append('-');
                }
                sb.append((char) (ch - 'A' + 'a'));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static void enableAll(Game g) {
        BlackCard.enable(g);
        RedCard.enable(g);
        ControlledDice.enable(g);
        StayCard.enable(g);
        Roadblock.enable(g);
        ReverseCard.enable(g);
        TortoiseCard.enable(g);
        TaxCard.enable(g);
        EqualWealthCard.enable(g);
        LotteryCard.enable(g);
        MonsterCard.enable(g);
        RobCard.enable(g);
        TeardownCard.enable(g);
        BuyLandCard.enable(g);
        GodOfLandCard.enable(g);
        GodOfFortuneCard.enable(g);
        GodOfLuckCard.enable(g);
    }
}
