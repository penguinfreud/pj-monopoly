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
    public void askWhetherToBuyProperty(Consumer1<Boolean> cb) {
        cb.run(true);
    }

    @Override
    public void askWhetherToUpgradeProperty(Consumer1<Boolean> cb) {
        cb.run(true);
    }

    @Override
    public void askWhichPropertyToMortgage(Consumer1<Property> cb) {
        cb.run(getProperties().get(0));
    }

    @Override
    protected void askWhichCardToBuy(Consumer1<Card> cb) {
        int coupons = getCoupons();
        if (coupons == 0) {
            cb.run(null);
        } else {
            Object[] buyableCards = Card.getCards().stream().filter((card) -> card.getPrice(getGame()) <= coupons).toArray();
            if (buyableCards.length == 0) {
                cb.run(null);
            } else {
                cb.run((Card) buyableCards[ThreadLocalRandom.current().nextInt(buyableCards.length)]);
            }
        }
    }

    @Override
    public void startTurn(Consumer0 cb) {
        List<Card> cards = getCards();
        if (cards.isEmpty()) {
            cb.run();
        } else {
            Card card = cards.get(ThreadLocalRandom.current().nextInt(cards.size()));
            useCard(card, () -> startTurn(cb));
        }
    }

    @Override
    public void askHowMuchToDepositOrWithdraw(Consumer1<Integer> cb) {
        cb.run(-getDeposit());
    }

    @Override
    public void askForPlayer(String reason, Consumer1<AbstractPlayer> cb) {
        if (reason.equals("ReverseCard")) {
            cb.run(this);
        } else {
            List<AbstractPlayer> players = getGame().getPlayers();
            AbstractPlayer first = players.get(0);
            cb.run(first == this? players.get(1): first);
        }
    }

    @Override
    public void askForPlace(String reason, Consumer1<Place> cb) {
        Place cur = getCurrentPlace();
        if (reason.equals("Roadblock")) {
            cb.run(cur);
        } else {
            cb.run(isReversed() ? cur.getPrev() : cur.getNext());
        }
    }
}
