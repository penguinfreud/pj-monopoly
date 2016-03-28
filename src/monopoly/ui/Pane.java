package monopoly.ui;

import javax.swing.*;
import java.io.Serializable;

public abstract class Pane implements Serializable {
    protected JComponent comp;
    protected MainController controller;

    Pane(MainController controller) {
        this.controller = controller;
    }

    protected abstract void onEnter();
    protected abstract void onLeave();
}
