package monopoly;

import monopoly.util.Callback;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class AIPlayer extends AbstractPlayer {
    public AIPlayer() {}

    public AIPlayer(String name) {
        setName(name);
    }

    @Override
    public void askWhetherToBuyProperty(Game g, Callback<Boolean> cb) {
        cb.run(g, true);
    }

    @Override
    public void askWhetherToUpgradeProperty(Game g, Callback<Boolean> cb) {
        cb.run(g, true);
    }

    @Override
    public void askWhichPropertyToMortgage(Game g, Callback<Property> cb) {
        cb.run(g, getProperties().get(0));
    }

    @Override
    protected void askWhichCardToBuy(Game g, Callback<Card> cb) {
        int coupons = getCoupons();
        if (coupons == 0) {
            cb.run(g, null);
        } else {
            List<Card> cards = Card.getCards();
            List<Card> buyableCards = new CopyOnWriteArrayList<>();
            for (Card card: cards) {
                if (card.getPrice(g) <= coupons) {
                    buyableCards.add(card);
                }
            }
            if (buyableCards.isEmpty()) {
                cb.run(g, null);
            } else {
                cb.run(g, buyableCards.get(ThreadLocalRandom.current().nextInt(buyableCards.size())));
            }
        }
    }

    @Override
    public void startTurn(Game g, Callback<Object> cb) {
        List<Card> cards = getCards();
        if (cards.isEmpty()) {
            cb.run(g, null);
        } else {
            Card card = cards.get(ThreadLocalRandom.current().nextInt(cards.size()));
            useCard(g, card, (_g, o) -> startTurn(_g, cb));
        }
    }

    @Override
    public void askHowMuchToDepositOrWithdraw(Game g, Callback<Integer> cb) {
        cb.run(g, -getDeposit());
    }

    @Override
    public void askForPlayer(Game g, String reason, Callback<AbstractPlayer> cb) {
        if (reason.equals("ReverseCard")) {
            cb.run(g, this);
        } else {
            List<AbstractPlayer> players = g.getPlayers();
            AbstractPlayer first = players.get(0);
            cb.run(g, first == this? players.get(1): first);
        }
    }

    @Override
    public void askForPlace(Game g, String reason, Callback<Place> cb) {
        Place cur = getCurrentPlace();
        if (reason.equals("Roadblock")) {
            cb.run(g, cur);
        } else {
            cb.run(g, isReversed() ? cur.getPrev() : cur.getNext());
        }
    }
}
