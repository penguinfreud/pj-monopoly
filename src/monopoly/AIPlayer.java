package monopoly;

import monopoly.card.Card;
import monopoly.util.Consumer0;
import monopoly.util.Consumer1;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class AIPlayer extends AbstractPlayer {
    public AIPlayer() {}

    public AIPlayer(String name) {
        setName(name);
    }

    @Override
    public void askWhetherToBuyProperty(Game g, Consumer1<Boolean> cb) {
        cb.run(true);
    }

    @Override
    public void askWhetherToUpgradeProperty(Game g, Consumer1<Boolean> cb) {
        cb.run(true);
    }

    @Override
    public void askWhichPropertyToMortgage(Game g, Consumer1<Property> cb) {
        cb.run(getProperties().get(0));
    }

    @Override
    protected void askWhichCardToBuy(Game g, Consumer1<Card> cb) {
        int coupons = getCoupons();
        if (coupons == 0) {
            cb.run(null);
        } else {
            Object[] buyableCards = Card.getCards().stream().filter((card) -> card.getPrice(g) <= coupons).toArray();
            if (buyableCards.length == 0) {
                cb.run(null);
            } else {
                cb.run((Card) buyableCards[ThreadLocalRandom.current().nextInt(buyableCards.length)]);
            }
        }
    }

    @Override
    public void startTurn(Game g, Consumer0 cb) {
        List<Card> cards = getCards();
        if (cards.isEmpty()) {
            cb.run();
        } else {
            Card card = cards.get(ThreadLocalRandom.current().nextInt(cards.size()));
            useCard(g, card, () -> startTurn(g, cb));
        }
    }

    @Override
    public void askHowMuchToDepositOrWithdraw(Game g, Consumer1<Integer> cb) {
        cb.run(-getDeposit());
    }

    @Override
    public void askForPlayer(Game g, String reason, Consumer1<AbstractPlayer> cb) {
        if (reason.equals("ReverseCard")) {
            cb.run(this);
        } else {
            List<AbstractPlayer> players = g.getPlayers();
            AbstractPlayer first = players.get(0);
            cb.run(first == this? players.get(1): first);
        }
    }

    @Override
    public void askForPlace(Game g, String reason, Consumer1<Place> cb) {
        Place cur = getCurrentPlace();
        if (reason.equals("Roadblock")) {
            cb.run(cur);
        } else {
            cb.run(isReversed() ? cur.getPrev() : cur.getNext());
        }
    }
}
