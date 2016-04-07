package monopoly;

import monopoly.util.Consumer0;
import monopoly.util.Consumer1;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AIPlayer extends AbstractPlayer implements Properties.IPlayerWithProperties, Cards.IPlayerWithCards {
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
        cb.run(Properties.get(this).getProperties().get(0));
    }

    @Override
    public void askWhichCardToBuy(Consumer1<Card> cb) {
        int coupons = Cards.get(this).getCoupons();
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
        Cards cards = Cards.get(this);
        List<Card> cardList = cards.getCards();
        if (cardList.isEmpty()) {
            cb.run();
        } else {
            Card card = cardList.get(ThreadLocalRandom.current().nextInt(cardList.size()));
            cards.useCard(card, () -> startTurn(cb));
        }
    }

    @Override
    public void askHowMuchToDepositOrWithdraw(Consumer1<Integer> cb) {
        cb.run(-getDeposit());
    }

    @Override
    public void askForTargetPlayer(String reason, Consumer1<AbstractPlayer> cb) {
        if (reason.equals("ReverseCard")) {
            cb.run(this);
        } else {
            List<AbstractPlayer> players = getGame().getPlayers();
            AbstractPlayer first = players.get(0);
            cb.run(first == this? players.get(1): first);
        }
    }

    @Override
    public void askForTargetPlace(String reason, Consumer1<Place> cb) {
        Place cur = getCurrentPlace();
        if (reason.equals("Roadblock")) {
            cb.run(cur);
        } else {
            cb.run(isReversed() ? cur.getPrev() : cur.getNext());
        }
    }
}
