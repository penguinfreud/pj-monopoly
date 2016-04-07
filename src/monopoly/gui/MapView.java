package monopoly.gui;

import monopoly.IPlayer;

import java.awt.Graphics;
import javax.swing.*;

public class MapView extends Pane {
    private class MapPanel extends JPanel {
        @Override
        public void paint(Graphics g) {
            drawBackground(g);
            
            GUIPlace start = (GUIPlace) controller.getGame().getMap().getStartingPoint();
            GUIPlace place = start;
            do {
                place.draw(g);
                place = (GUIPlace) place.getNext();
            } while(place != start);
            
            for (IPlayer player: controller.getGame().getPlayers()) {
                drawPlayer(g, player);
            }
        }
        
        private void drawBackground(Graphics g) {
        
        }
        
        private void drawPlayer(Graphics g, IPlayer player) {
        
        }
    }
    
    public MapView(MainController controller) {
        super(controller);
        comp = new MapPanel();
    }
}
