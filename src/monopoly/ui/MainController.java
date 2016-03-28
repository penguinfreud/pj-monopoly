package monopoly.ui;

import monopoly.AbstractPlayer;
import monopoly.Game;
import monopoly.Map;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class MainController {
    Pane welcomePane, newGamePane, newPlayerPane, gamePane, gameOverPane;
    Pane currentPane;
    JFrame main;
    List<AbstractPlayer> players = new ArrayList<>();
    Map map;
    Game game;

    public MainController() {
        main = new MainFrame(this);
        welcomePane = new WelcomePane(this);
        newGamePane = new NewGamePane(this);
        newPlayerPane = new NewPlayerPane(this);
        gamePane = new GamePane(this);
        gameOverPane = new GameOverPane(this);
        switchTo(welcomePane);
        main.setVisible(true);
    }

    protected void switchTo(Pane newPane) {
        if (currentPane != null) {
            currentPane.onLeave();
        }
        currentPane = newPane;
        newPane.onEnter();
        main.setContentPane(newPane.comp);
        main.pack();
        main.repaint();
    }
}
