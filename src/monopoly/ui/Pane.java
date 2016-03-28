package monopoly.ui;

import javax.swing.*;

public abstract class Pane {
    protected JComponent comp;
    protected MainController controller;

    Pane(MainController controller) {
        this.controller = controller;
    }

    protected void onEnter() {}
    protected void onLeave() {}
}
