package monopoly;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import monopoly.place.Place;
import monopoly.util.Consumer0;
import monopoly.util.Consumer1;

import java.util.function.Supplier;

public interface IPlayer extends GameObject {
    Game getGame();

    StringProperty nameProperty();

    default String getName() {
        return nameProperty().get();
    }

    default void setName(String name) {
        nameProperty().set(name);
    }

    ObjectProperty<Place> currentPlaceProperty();

    default Place getCurrentPlace() {
        return currentPlaceProperty().get();
    }

    boolean isReversed();

    void reverse();

    DoubleProperty cashProperty();

    default double getCash() {
        return cashProperty().get();
    }

    DoubleProperty depositProperty();

    default double getDeposit() {
        return depositProperty().get();
    }

    DoubleBinding totalPossessions();

    default double getTotalPossessions() {
        return totalPossessions().get();
    }

    void addPossession(Supplier<Double> possession);

    void addPropertySeller(Consumer1<Consumer0> cb);

    void init();

    void giveUp();

    default void changeCash(double amount, String msg) {
        synchronized (getGame().lock) {
            if (getCash() + amount >= 0 || getCash() < 0 && amount >= 0) {
                cashProperty().set(getCash() + amount);
                triggerOnMoneyChange(amount, msg);
            } else {
                getGame().triggerException("short_of_cash");
            }
        }
    }

    default void changeDeposit(double amount, String msg) {
        synchronized (getGame().lock) {
            if (getDeposit() + amount >= 0) {
                depositProperty().set(getDeposit() + amount);
                triggerOnMoneyChange(amount, msg);
            } else {
                getGame().triggerException("short_of_deposit");
            }
        }
    }

    default void depositOrWithdraw(Consumer0 cb) {
        askHowMuchToDepositOrWithdraw((amount) -> {
            double maxTransfer = getGame().getConfig("bank-max-transfer");
            if (-maxTransfer <= amount && amount <= maxTransfer) {
                if (getCash() - amount >= 0 && getDeposit() + amount >= 0) {
                    cashProperty().set(getCash() - amount);
                    depositProperty().set(getDeposit() + amount);
                }
            } else {
                getGame().triggerException("exceeded_max_transfer_credits");
            }
            cb.accept();
        });
    }

    void pay(IPlayer receiver, double amount, String msg, Consumer0 cb);

    void triggerOnMoneyChange(double amount, String msg);

    void triggerBankrupt();

    void startWalking(int steps);

    default void startTurn(Consumer0 cb) {
        cb.accept();
    }

    default void askHowMuchToDepositOrWithdraw(Consumer1<Double> cb) {
        cb.accept(0.0);
    }
}
