package monopoly.ui;

import monopoly.AIPlayer;

import javax.swing.*;

public class NewPlayerPane extends Pane {
    private JTextField txtName;

    public NewPlayerPane(MainController controller) {
        super(controller);
        comp = Box.createVerticalBox();

        comp.add(new JLabel("Enter Player's Name:"));

        txtName = new JTextField();
        comp.add(txtName);

        JButton btnAddPlayer = new JButton("OK");
        comp.add(btnAddPlayer);
        btnAddPlayer.addActionListener((e) -> {
            controller.players.add(new AIPlayer(txtName.getText()));
            controller.switchTo(controller.newGamePane);
        });
    }

    @Override
    protected void onEnter() {}

    @Override
    protected void onLeave() {
        txtName.setText("");
    }
}
