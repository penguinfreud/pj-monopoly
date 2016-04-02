package monopoly.place;

import monopoly.*;
import monopoly.util.Callback;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CardSite extends Place {
    static {
        Map.registerPlaceReader("CardSite", (r, sc) -> new CardSite());
    }

    private CardSite() {
        super("CardSite");
    }

    @Override
    public void onLanded(Game g, AbstractPlayer.PlaceInterface pi, Callback<Object> cb) {
        List<Card> cards = Card.getCards();
        int l = cards.size();
        int[] prob = new int[l + 1];
        int sum = 0;
        for (int i = 0; i<l; i++) {
            sum += 128 / cards.get(i).getPrice(g);
            prob[i] = sum;
        }
        sum += 32;
        prob[l] = sum;

        int index = Arrays.binarySearch(prob, ThreadLocalRandom.current().nextInt(sum));
        if (index < 0) {
            index = -index - 1;
        }
        if (index == l) {
            cb.run(g, null);
        } else {
            pi.addCard(g.getCurrentPlayer(), g, cards.get(index));
            cb.run(g, null);
        }
    }
}
