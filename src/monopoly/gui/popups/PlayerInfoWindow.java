package monopoly.gui.popups;

import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import monopoly.Cards;
import monopoly.Game;
import monopoly.IPlayer;
import monopoly.Properties;
import monopoly.gui.MainController;

public class PlayerInfoWindow extends Stage {
    public PlayerInfoWindow(MainController controller) {
        VBox root = new VBox();
        Scene scene = new Scene(root, 400, 200);
        setScene(scene);

        Game g = controller.getGame();
        TableView<IPlayer> tableView = new TableView<>();
        TableColumn<IPlayer, String>
                nameCol = new TableColumn<>(controller.getText("name")),
                cashCol = new TableColumn<>(controller.getText("cash")),
                depositCol = new TableColumn<>(controller.getText("deposit")),
                totalPossessionsCol = new TableColumn<>(controller.getText("total_possessions"));
        TableColumn<IPlayer, Number>
                couponsCol = new TableColumn<>(controller.getText("coupons")),
                cardsCol = new TableColumn<>(controller.getText("cards")),
                propertiesCol = new TableColumn<>(controller.getText("properties"));
        nameCol.setCellValueFactory(df -> df.getValue().nameProperty());
        cashCol.setCellValueFactory(df -> df.getValue().cashProperty().asString("%.2f"));
        depositCol.setCellValueFactory(df -> df.getValue().depositProperty().asString("%.2f"));
        couponsCol.setCellValueFactory(df -> Cards.get(df.getValue()).couponsProperty());
        cardsCol.setCellValueFactory(df -> Bindings.size(Cards.get(df.getValue()).getCards()));
        propertiesCol.setCellValueFactory(df -> Bindings.size(Properties.get(df.getValue()).getProperties()));
        totalPossessionsCol.setCellValueFactory(df -> df.getValue().totalPossessions().asString("%.2f"));
        tableView.getColumns().add(cashCol);
        tableView.getColumns().add(depositCol);
        tableView.getColumns().add(couponsCol);
        tableView.getColumns().add(cardsCol);
        tableView.getColumns().add(propertiesCol);
        tableView.getColumns().add(totalPossessionsCol);

        tableView.setItems(g.getPlayers());
        root.getChildren().addAll(tableView);
    }
}
