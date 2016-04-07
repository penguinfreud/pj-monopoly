package monopoly;

import monopoly.util.Consumer0;
import monopoly.util.Consumer1;
import monopoly.util.Host;

import java.io.Serializable;

public interface IPlayer extends Serializable, Host, GameObject {
    String getName();
    Game getGame();
    int getCash();
    int getDeposit();
    int getTotalPossessions();
    boolean isReversed();
    Place getCurrentPlace();
    void reverse();
    void giveUp();
    void changeCash(int amount, String msg);
    void changeDeposit(int amount, String msg);
    void depositOrWithdraw(Consumer0 cb);
    void pay(AbstractPlayer receiver, int amount, String msg, Consumer0 cb);
    void askHowMuchToDepositOrWithdraw(Consumer1<Integer> cb);
}
