package monopoly.gui;

import javafx.beans.binding.Bindings;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import monopoly.Game;

import java.util.Hashtable;
import java.util.Map;

public class DiceView extends ImageView {
    private static final Map<MainController, DiceView> parasites = new Hashtable<>();

    public static DiceView get(MainController controller) {
        DiceView view = parasites.get(controller);
        if (view == null) {
            view = new DiceView(controller);
            parasites.put(controller, view);
        }
        return view;
    }

    private MainController controller;

    public DiceView(MainController controller) {
        this.controller = controller;
        Game g = controller.getGame();
        imageProperty().bind(Bindings.createObjectBinding(
                () -> getDiceImage(g.getDice()), g.diceProperty()));
    }

    private Image getDiceImage(int index) {
        return controller.getImageManager().getImage("/icons/dices/" + index + ".png");
    }
}
