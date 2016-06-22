package monopoly.gui;

import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import monopoly.Config;
import monopoly.Game;
import monopoly.IPlayer;

import java.io.File;

public class GameEditorPane extends VBox implements IPane {
    static {
        MainController.putDefaultConfig("selected-default-map-index", 1);
        MainController.putDefaultConfig("map-file", "");
    }

    private final MainController controller;
    private HBox bottomPane = new HBox();

    public GameEditorPane(MainController controller) {
        this.controller = controller;

        getChildren().addAll(createPlayerList(), createMapChooser(), createDurationChooser());
        createBottom();
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(30));
    }

    @Override
    public Node bottom() {
        return bottomPane;
    }

    @Override
    public Node center() {
        return this;
    }

    private class PlayerListCell extends ListCell<IPlayer> {
        private StringProperty oldName = null;

        public PlayerListCell() {
            setFont(controller.getConfig().get("font"));
        }

        @Override
        protected void updateItem(IPlayer item, boolean empty) {
            super.updateItem(item, empty);
            if (oldName != null) {
                textProperty().unbindBidirectional(oldName);
            }
            if (item == null || empty) {
                setText("");
            } else {
                oldName = item.nameProperty();
                textProperty().bindBidirectional(oldName);
            }
        }
    }

    private VBox createPlayerList() {
        VBox vBox = new VBox();
        HBox buttons = new HBox();

        Game g = controller.getGame();

        ListView<IPlayer> listView = new ListView<>();
        listView.setCellFactory(lv -> new PlayerListCell());
        listView.setItems(g.getPlayers());
        listView.setPrefWidth(200);
        listView.setPrefHeight(200);
        listView.setStyle(controller.getCSSFont());

        Button addBtn = controller.createButton("add_a_human_player",
                e -> {
                    IPlayer player = new GUIPlayer(g, controller);
                    g.addPlayer(player);
                    controller.editPlayer(player);
                });

        Button editBtn = controller.createButton("edit",
                e -> {
                    IPlayer player = listView.getSelectionModel().getSelectedItem();
                    if (player != null)
                        controller.editPlayer(player);
                });

        Button removeBtn = controller.createButton("remove",
                e -> {
                    IPlayer player = listView.getSelectionModel().getSelectedItem();
                    if (player != null)
                        g.removePlayer(player);
                });

        buttons.getChildren().addAll(addBtn, editBtn, removeBtn);
        buttons.setSpacing(5);

        vBox.getChildren().addAll(buttons, listView);
        vBox.setPadding(new Insets(10));
        vBox.setSpacing(10);

        return vBox;
    }

    private VBox createMapChooser() {
        VBox vBox = new VBox();

        ComboBox<String> comboBox = new ComboBox<>();
        Config config = controller.getConfig();
        String[] defaultMaps = config.get("default-maps");

        comboBox.getItems().add(controller.getText(defaultMaps[0]));
        for (int i = 1; i < defaultMaps.length; i++) {
            comboBox.getItems().add(defaultMaps[i]);
        }

        SelectionModel<String> selectionModel = comboBox.getSelectionModel();
        String key = "selected-default-map-index";
        selectionModel.select((int) config.get(key));

        VBox fromFilePane = new VBox();

        Text chosenFile = new Text();
        chosenFile.textProperty().bind(config.stringValueAt("map-file"));

        Button chooseBtn = controller.createButton("choose_file",
                e -> {
                    FileChooser fileChooser = new FileChooser();
                    File selectedFile = fileChooser.showOpenDialog(controller.getStage());
                    controller.getConfig().put("map-file",
                            selectedFile == null ? "" : selectedFile.getAbsolutePath());
                });

        fromFilePane.getChildren().addAll(chosenFile, chooseBtn);
        fromFilePane.setVisible((Integer) config.get(key) == 0);

        selectionModel.selectedIndexProperty().addListener(
                (observable, oldValue, newValue) -> {
                    config.put(key, newValue.intValue());
                    if (newValue.intValue() == 0) {
                        fromFilePane.setVisible(true);
                    } else {
                        fromFilePane.setVisible(false);
                    }
                });

        vBox.getChildren().addAll(controller.createText("map_colon"), comboBox, fromFilePane);
        vBox.setPadding(new Insets(10));
        vBox.setSpacing(10);

        return vBox;
    }

    private VBox createDurationChooser() {
        VBox vBox = new VBox();
        ComboBox<Integer> comboBox = new ComboBox<>();
        ObservableList<Integer> items = FXCollections.observableArrayList();
        items.addAll(-1, 7, 14, 30, 60, 365);
        comboBox.setItems(items);
        comboBox.setConverter(new StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                if (object == -1) {
                    return(controller.getText("unlimited"));
                } else {
                    return(object.toString() + controller.getText("days"));
                }
            }

            @Override
            public Integer fromString(String string) {
                return -1;
            }
        });
        comboBox.getSelectionModel().select(items.get(0));
        comboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> controller.getConfig().put("duration", newValue));
        vBox.getChildren().addAll(controller.createText("duration_colon"), comboBox);

        vBox.setPadding(new Insets(10));
        vBox.setSpacing(10);
        return vBox;
    }

    private void createBottom() {
        Button startBtn = controller.createButton("start_game", e -> controller.startGame());
        Button backBtn = controller.createButton("back", e -> controller.welcome());

        bottomPane.getChildren().addAll(startBtn, backBtn);
        bottomPane.setAlignment(Pos.BOTTOM_RIGHT);
        bottomPane.setPadding(new Insets(20));
        bottomPane.setSpacing(10);
    }
}
