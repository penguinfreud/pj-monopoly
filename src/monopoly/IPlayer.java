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
    double getCash();
    double getDeposit();
    double getTotalPossessions();
    void addPossession(Supplier<Double> possession);
    void addPropertySeller(Consumer1<Consumer0> cb);

    void init();
    void giveUp();
    void changeCash(double amount, String msg);
    void changeDeposit(double amount, String msg);
    void depositOrWithdraw(Consumer0 cb);
    void pay(IPlayer receiver, double amount, String msg, Consumer0 cb);
    void startWalking(int steps);

    default void startTurn(Consumer0 cb) {
        cb.run();
    }

    default void askHowMuchToDepositOrWithdraw(Consumer1<Double> cb) {
        cb.run(0.0);
    }
}
