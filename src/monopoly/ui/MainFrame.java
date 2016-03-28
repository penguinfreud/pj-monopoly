package monopoly.ui;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {
    private MainController controller;

    public MainFrame(MainController controller) {
        this.controller = controller;

        setTitle("Monopoly");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int ret = JOptionPane.showConfirmDialog(null, "Do you really want to exit Monopoly?", "Confirm exit", JOptionPane.YES_NO_OPTION);
                if (ret == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
    }
}
