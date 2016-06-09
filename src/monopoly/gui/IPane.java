package monopoly.gui;

import javafx.scene.Node;

public interface IPane {
    default Node top() {
        return null;
    }

    default Node left() {
        return null;
    }

    default Node right() {
        return null;
    }

    default Node bottom() {
        return null;
    }

    Node center();

    default void onShow() {}

    default void onHide() {}
}
