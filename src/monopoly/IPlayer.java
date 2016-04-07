package monopoly;

import monopoly.util.Consumer0;
import monopoly.util.Consumer1;
import monopoly.util.Host;

import java.io.Serializable;

public interface IPlayer extends Serializable, Host, GameObject {
    Game getGame();
    void setGame(Game g);
    String getName();
    Place getCurrentPlace();
    boolean isReversed();
    void reverse();
    int getCash();
    int getDeposit();
    int getTotalPossessions();

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
