package monopoly.ui;

import javax.swing.*;

public class GamePane extends Pane {
    public GamePane(MainController controller) {
        super(controller);
        comp = new JPanel();
        comp.add(new JLabel("Hello"));
    }

    @Override
    protected void onEnter() {}

    @Override
    protected void onLeave() {}
}
