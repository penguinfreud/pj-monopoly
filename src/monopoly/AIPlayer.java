package monopoly;

import monopoly.async.Callback;

import java.util.List;
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
        List<Card> cards = Card.getCards();
        cb.run(g, cards.get(ThreadLocalRandom.current().nextInt(cards.size())));
    }

    @Override
    public void askWhichCardToUse(Game g, Callback<Card> cb) {
        cb.run(g, null);
    }

    @Override
    public void askHowMuchToDepositOrWithdraw(Game g, Callback<Integer> cb) {
        cb.run(g, 0);
    }

    @Override
    public void askWhomToReverse(Game g, Callback<AbstractPlayer> cb) {
        cb.run(g, this);
    }

    @Override
    public void askWhereToGo(Game g, Callback<Place> cb) {
        Place cur = getCurrentPlace();
        cb.run(g, isReversed()? cur.getPrev(): cur.getNext());
    }

    @Override
    public void askWhereToSetRoadblock(Game g, Callback<Place> cb) {
        cb.run(g, getCurrentPlace());
    }
}
