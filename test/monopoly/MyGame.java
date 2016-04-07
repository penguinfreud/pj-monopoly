package monopoly;

import java.util.ArrayList;
import java.util.List;

public class MyGame extends Game {
    private int turnNo = 0;
    int forwardUntil = 0;

    @Override
    public void startWalking() {
        if (forwardUntil > 0 || forwardUntil == -1) {
            if (turnNo < forwardUntil || forwardUntil == -1) {
                super.startWalking();
            }
        }
    }
}
