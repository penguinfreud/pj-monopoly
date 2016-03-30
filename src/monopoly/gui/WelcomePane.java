package monopoly.gui;

import javax.swing.*;

public class WelcomePane extends Pane {
    public WelcomePane(MainController controller) {
        super(controller);
        comp = new JPanel();
        comp.add(createNewGameBtn());
    }
    
    private JComponent createNewGameBtn() {
        JButton btnNewGame = new JButton("New Game");
        btnNewGame.addActionListener((e) -> controller.newGame());
        return btnNewGame;
    }
}
