package monopoly.gui;

import monopoly.*;
import monopoly.async.Callback;

public class GUIPlayer extends AbstractPlayer {
    public GUIPlayer() {}

    public GUIPlayer(String name) {
        setName(name);
    }

    @Override
    public void askWhetherToBuyProperty(Game g, Callback<Boolean> cb) {

    }

    @Override
    public void askWhetherToUpgradeProperty(Game g, Callback<Boolean> cb) {

    }

    @Override
    public void askWhichPropertyToMortgage(Game g, Callback<Property> cb) {

    }

    @Override
    public void askWhomToReverse(Game g, Callback<AbstractPlayer> cb) {
        cb.run(this);
    }

    @Override
    public void askWhichCardToUse(Game g, Callback<Card> cb) {
        cb.run(null);
    }

    @Override
    public void askHowMuchToDepositOrWithdraw(Game g, Callback<Integer> cb) {
        cb.run(0);
    }

    @Override
    public void askWhereToGo(Game g, Callback<Place> cb) {
        Place cur = getCurrentPlace();
        cb.run(isReversed()? cur.getPrev(): cur.getNext());
    }

    @Override
    public void askWhereToSetRoadblock(Game g, Callback<Place> cb) {
        cb.run(getCurrentPlace());
    }
}
