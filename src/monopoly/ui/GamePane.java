package monopoly.ui;

import javax.swing.*;

public class GamePane extends Pane {
    private MapView mapView;
    private PlayerView playerView;
    
    public GamePane(MainController controller) {
        super(controller);
        comp = Box.createHorizontalBox();
        comp.add(createMapView());
        comp.add(createPlayerView());
    }
    
    private JComponent createMapView() {
        mapView = new MapView(controller);
        return mapView.comp;
    }
    
    private JComponent createPlayerView() {
        playerView = new PlayerView(controller);
        return playerView.comp;
    }
}
