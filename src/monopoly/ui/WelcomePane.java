package monopoly.ui;

import javax.swing.*;

public class WelcomePane extends Pane {
    public WelcomePane(MainController controller) {
        super(controller);
        comp = new JPanel();
        JButton btnNewGame = new JButton("New Game");
        btnNewGame.addActionListener((e) -> controller.switchTo(controller.newGamePane));
        comp.add(btnNewGame);
    }

    @Override
    protected void onEnter() {}

    @Override
    protected void onLeave() {}
}
