package monopoly.gui;

import monopoly.AbstractPlayer;

import javax.swing.*;

public class NewGamePane extends Pane {
    private JLabel playersDisplay;

    public NewGamePane(MainController controller) {
        super(controller);
        comp = Box.createVerticalBox();
        comp.add(createPlayersDisplay());
        comp.add(createNewPlayerBtn());
        comp.add(createStartGameBtn());
    }
    
    private JComponent createPlayersDisplay() {
        playersDisplay = new JLabel(" ");
        return playersDisplay;
    }
    
    private JComponent createNewPlayerBtn() {
        JButton btnNewPlayer = new JButton("New Player");
        btnNewPlayer.addActionListener((e) -> controller.newPlayer());
        return btnNewPlayer;
    }
    
    private JComponent createMapSelector() {
        return null;
    }
    
    private JComponent createStartGameBtn() {
        JButton btnStartGame = new JButton("Start Game");
        btnStartGame.addActionListener((e) -> controller.startGame());
        return btnStartGame;
    }

    private void updatePlayerNames() {
        synchronized (controller.lock) {
            StringBuilder sb = new StringBuilder();
            for (AbstractPlayer player : controller.getPlayers()) {
                sb.append(player.getName());
                sb.append(' ');
            }
            playersDisplay.setText(sb.toString());
            controller.pack();
        }
    }

    @Override
    protected void onEnter() {
        updatePlayerNames();
    }
}
