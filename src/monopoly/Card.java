package monopoly;

import monopoly.util.Callback;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
}
