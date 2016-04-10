package monopoly.gui;

import monopoly.BasePlayer;

import javax.swing.*;

public class NewPlayerPane extends Pane {
    private JTextField txtName;

    public NewPlayerPane(MainController controller) {
        super(controller);
        comp = Box.createVerticalBox();
        comp.add(createNameField());
        comp.add(createOKBtn());
    }
    
    private JComponent createNameField() {
        Box box = Box.createVerticalBox();
        box.add(new JLabel("Enter Player's Name:"));
        txtName = new JTextField();
        box.add(txtName);
        return txtName;
    }
    
    private JComponent createOKBtn() {
        JButton btnOk = new JButton("OK");
        btnOk.addActionListener((e) -> {
            synchronized (controller.lock) {
                controller.addPlayer(new BasePlayer(txtName.getText(), controller.getGame()));
            }
        });
        return btnOk;
    }

    @Override
    protected void onLeave() {
        txtName.setText("");
    }
}
