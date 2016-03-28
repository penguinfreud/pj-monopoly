package monopoly.ui;

import monopoly.AIPlayer;
import monopoly.AbstractPlayer;

import javax.swing.*;

public class NewGamePane extends Pane {
    private JLabel playersDisplay;

    public NewGamePane(MainController controller) {
        super(controller);
        comp = Box.createVerticalBox();

        playersDisplay = new JLabel(" ");
        comp.add(playersDisplay);

        JButton btnNewPlayer = new JButton("New Player");
        comp.add(btnNewPlayer);
        btnNewPlayer.addActionListener((e) -> controller.switchTo(controller.newPlayerPane));

        JButton btnStartGame = new JButton("Start Game");
        comp.add(btnStartGame);
        btnStartGame.addActionListener((e) -> controller.switchTo(controller.gamePane));
    }

    private void updatePlayerNames() {
        StringBuffer sb = new StringBuffer();
        for (AbstractPlayer player: controller.players) {
            sb.append(player.getName());
            sb.append(' ');
        }
        playersDisplay.setText(sb.toString());
        controller.main.pack();
    }

    @Override
    protected void onEnter() {
        updatePlayerNames();
    }

    @Override
    protected void onLeave() {}
}
