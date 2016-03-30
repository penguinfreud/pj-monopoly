package monopoly.gui;

import javax.swing.*;

public abstract class Pane {
    protected JComponent comp;
    protected MainController controller;

    protected Pane(MainController controller) {
        this.controller = controller;
    }

    protected void onEnter() {}
    protected void onLeave() {}
}
