package monopoly.gui;

import monopoly.GameMap;
import monopoly.IPlayer;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MainController {
    final Object lock = new Object();

    private Pane welcomePane, newGamePane, newPlayerPane, gamePane, gameOverPane;
    private JFrame main;
    private List<IPlayer> players;
    private File mapFile;
    private GUIGame game;
    
    private Pane currentPane;

    public MainController() {
        createFrame();
        welcomePane = new WelcomePane(this);
        newGamePane = new NewGamePane(this);
        newPlayerPane = new NewPlayerPane(this);
        gamePane = new GamePane(this);
        gameOverPane = new GameOverPane(this);
        
        switchTo(welcomePane);
        main.setVisible(true);
    }
    
    private void createFrame() {
        main = new JFrame();
        main.setTitle("Monopoly");
        main.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        main.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int ret = JOptionPane.showConfirmDialog(null, "Do you really want to exit Monopoly?", "Confirm exit", JOptionPane.YES_NO_OPTION);
                if (ret == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
    }

    private void switchTo(Pane newPane) {
        synchronized (lock) {
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
    
    public GUIGame getGame() {
        return game;
    }
    
    public List<IPlayer> getPlayers() {
        return new CopyOnWriteArrayList<>(players);
    }
    
    public void newGame() {
        synchronized (lock) {
            if (currentPane == welcomePane) {
                players = new CopyOnWriteArrayList<>();
                switchTo(newGamePane);
            }
        }
    }
    
    public void newPlayer() {
        synchronized (lock) {
            if (currentPane == newGamePane) {
                switchTo(newPlayerPane);
            }
        }
    }
    
    public void addPlayer(IPlayer player) {
        synchronized (lock) {
            if (currentPane == newPlayerPane) {
                players.add(player);
                switchTo(newGamePane);
            }
        }
    }
    
    public void startGame() {
        synchronized (lock) {
            if (currentPane == newGamePane &&
                mapFile != null && mapFile.exists() && players.size() >= 2) {
                try {
                    game = new GUIGame();
                    GameMap map = GameMap.readMap(new FileInputStream(mapFile));
                    game.setMap(map);
                    game.setPlayers(players);
                    switchTo(gamePane);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public void gameOver() {
        synchronized (lock) {
            if (currentPane == gamePane) {
                switchTo(gameOverPane);
            }
        }
    }

    public void welcome() {
        synchronized (lock) {
            if (currentPane == gameOverPane) {
                switchTo(welcomePane);
            }
        }
    }
    
    public void pack() {
        main.pack();
    }
}
