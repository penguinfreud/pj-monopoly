package monopoly;

import monopoly.async.Callback;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Card {
    private String name;

    protected Card(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public int getPrice(Game g) {
        return (Integer) g.getConfig("card-" + name.toLowerCase() + "-price");
    }

    public void use(Game g, AbstractPlayer.CardInterface ci, Callback<Object> cb) {
        cb.run(null);
    }

    private static final List<Card> cards = new CopyOnWriteArrayList<>();

    public static final void registerCard(Card card) {
        cards.add(card);
    }

    public static final List<Card> getCards() {
        return new CopyOnWriteArrayList<>(cards);
    }
}
