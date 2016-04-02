package monopoly;

import monopoly.util.Callback;

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
