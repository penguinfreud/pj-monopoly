package monopoly;

import monopoly.util.Callback;

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
        return (Integer) g.getConfig(name.toLowerCase() + "-price");
    }

    public void use(Game g, AbstractPlayer.CardInterface ci, Callback<Object> cb) {
        cb.run(g, null);
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
}
