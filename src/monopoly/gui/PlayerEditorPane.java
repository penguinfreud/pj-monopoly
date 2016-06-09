package monopoly.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

public class PlayerEditorPane extends VBox implements IPane {
    private MainController controller;
    private HBox bottomPane = new HBox();

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
        controller.editingPlayerProperty().addListener((v, ov, nv) -> {
            if (ov != null) {
                nameField.textProperty().unbindBidirectional(ov.nameProperty());
            }
            nameField.textProperty().bindBidirectional(nv.nameProperty());
        });
        nameField.setFont(controller.getConfig().get("font"));
        hBox.getChildren().addAll(controller.createText("Name: "), nameField);
        hBox.setAlignment(Pos.TOP_CENTER);
        hBox.setPadding(new Insets(10));
        return hBox;
    }

    private Pane createIconSelector() {
        FlowPane gridPane = new FlowPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        ToggleGroup toggleGroup = new ToggleGroup();
        for (int i = 1; i<=(Integer) controller.getConfig().get("icons-count"); i++) {
            ImageView imageView = new ImageView(controller.getImageManager().getImage("/icons/characters/96x96/" + i + ".png"));
            RadioButton radioButton = new RadioButton();
            radioButton.setToggleGroup(toggleGroup);
            radioButton.setGraphic(imageView);
            if (i == 1)
                radioButton.setSelected(true);
            gridPane.getChildren().add(radioButton);
        }
        return gridPane;
    }

    private void createBottomPane() {
        Button okBtn = controller.createButton("OK", e -> controller.completeEditingPlayer());

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
