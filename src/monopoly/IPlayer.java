package monopoly;

import monopoly.place.Place;
import monopoly.util.*;

import java.io.Serializable;

public interface IPlayer extends Serializable, Host, GameObject {
    Game getGame();
    String getName();
    Place getCurrentPlace();
    boolean isReversed();
    void reverse();
    int getCash();
    int getDeposit();
    int getTotalPossessions();
    void addPossession(Supplier<Integer> possession);
    void addPropertySeller(Consumer1<Consumer0> cb);

    void init();
    void giveUp();
    void changeCash(int amount, String msg);
    void changeDeposit(int amount, String msg);
    void depositOrWithdraw(Consumer0 cb);
    void pay(IPlayer receiver, int amount, String msg, Consumer0 cb);
    void startWalking(int steps);

    default void startTurn(Consumer0 cb) {
        cb.run();
    }

    default void askHowMuchToDepositOrWithdraw(Consumer1<Integer> cb) {
        cb.run(0);
    }
}
