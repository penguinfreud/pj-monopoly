package monopoly.gui;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import monopoly.IPlayer;

import java.util.ArrayList;
import java.util.List;

public class PlayerEditorPane extends VBox implements IPane {
    private MainController controller;
    private HBox bottomPane = new HBox();
    private List<RadioButton> iconButtons = new ArrayList<>();

    public PlayerEditorPane(MainController controller) {
        this.controller = controller;

        getChildren().addAll(createNameField(), createIconSelector());
        setPadding(new Insets(40));
        setSpacing(20);

        createBottomPane();
    }

    private HBox createNameField() {
        HBox hBox = new HBox();
        TextField nameField = new TextField();
        Util.bindBidirectional(nameField.textProperty(), controller.editingPlayerProperty(), IPlayer::nameProperty);
        nameField.setFont(controller.getConfig().get("font"));
        hBox.getChildren().addAll(controller.createText("name_colon"), nameField);
        hBox.setAlignment(Pos.TOP_CENTER);
        hBox.setPadding(new Insets(10));
        return hBox;
    }

    private Pane createIconSelector() {
        FlowPane gridPane = new FlowPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        ToggleGroup toggleGroup = new ToggleGroup();
        for (int i = 1; i <= (Integer) controller.getConfig().get("icons-count"); i++) {
            ImageView imageView = new ImageView(controller.getImageManager().getImage("/icons/characters/96x96/" + i + ".png"));
            RadioButton radioButton = new RadioButton();
            radioButton.setToggleGroup(toggleGroup);
            radioButton.setGraphic(imageView);
            radioButton.selectedProperty().addListener(changeIcon(i - 1));
            gridPane.getChildren().add(radioButton);
            iconButtons.add(radioButton);
        }
        controller.editingPlayerProperty().addListener((observable, oldValue, newValue) -> {
            iconButtons.get(GUIPlayerInfo.get(newValue).getIconIndex()).setSelected(true);
        });
        return gridPane;
    }

    private ChangeListener<Boolean> changeIcon(int index) {
        return (observable, oldValue, newValue) -> {
            if (newValue) {
                GUIPlayerInfo.get(controller.editingPlayerProperty().get()).setIconIndex(index);
            }
        };
    }

    private void createBottomPane() {
        Button okBtn = controller.createButton("ok", e -> controller.completeEditingPlayer());

        bottomPane.getChildren().addAll(okBtn);
        bottomPane.setAlignment(Pos.BOTTOM_RIGHT);
        bottomPane.setPadding(new Insets(20));
        bottomPane.setSpacing(10);
    }

    @Override
    public Node center() {
        return this;
    }

    @Override
    public Node bottom() {
        return bottomPane;
    }
}
