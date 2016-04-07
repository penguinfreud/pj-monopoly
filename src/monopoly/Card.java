package monopoly;

import monopoly.util.Consumer0;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Card implements Serializable, GameObject {
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

    protected void use(Game g, Consumer0 cb) {
        cb.run();
    }

    private static final List<Card> cards = new CopyOnWriteArrayList<>();

    protected static void registerCard(Card card) {
        cards.add(card);
    }

    public static List<Card> getCards() {
        return new CopyOnWriteArrayList<>(cards);
    }

    public static Card getRandomCard(Game g, boolean miss) {
        int l = cards.size();
        int[] prob = new int[l + 1];
        int sum = 0;
        for (int i = 0; i<l; i++) {
            sum += 128 / cards.get(i).getPrice(g);
            prob[i] = sum;
        }
        if (miss) {
            sum += 32;
            prob[l] = sum;
        }

        int index = Arrays.binarySearch(prob, ThreadLocalRandom.current().nextInt(sum));
        if (index < 0) {
            index = -index - 1;
        }
        if (index == l) {
            return null;
        } else {
            return cards.get(index);
        }
    }

    private String uncamelize(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i<str.length(); i++) {
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

    public static void loadAll() throws ClassNotFoundException {
        Class.forName("monopoly.card.BlackCard");
        Class.forName("monopoly.card.RedCard");
        Class.forName("monopoly.card.ControlledDice");
        Class.forName("monopoly.card.StayCard");
        Class.forName("monopoly.card.Roadblock");
        Class.forName("monopoly.card.ReverseCard");
        Class.forName("monopoly.card.TortoiseCard");
        Class.forName("monopoly.card.TaxCard");
        Class.forName("monopoly.card.EqualWealthCard");
        Class.forName("monopoly.card.LotteryCard");
        Class.forName("monopoly.card.MonsterCard");
        Class.forName("monopoly.card.RobCard");
        Class.forName("monopoly.card.TeardownCard");
        Class.forName("monopoly.card.BuyLandCard");
        Class.forName("monopoly.card.GodOfLandCard");
        Class.forName("monopoly.card.GodOfFortuneCard");
        Class.forName("monopoly.card.GodOfLuckCard");
    }
}
