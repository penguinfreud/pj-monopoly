package monopoly.gui.popups;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import monopoly.gui.MainController;
import monopoly.stock.Stock;
import monopoly.stock.StockMarket;

import java.util.stream.Stream;

public class StockWindow extends Stage {
    private MainController controller;

    public StockWindow(MainController controller) {
        this.controller = controller;
        VBox root = new VBox();
        Scene scene = new Scene(root, 400, 400);
        setScene(scene);

        root.getChildren().addAll(createStockTable());
    }

    private Node createTrendGraph() {
        return null;
    }

    private Node createStockTable() {
        StockMarket market = StockMarket.getMarket(controller.getGame());

        TableView<Stock> tableView = new TableView<>();
        TableColumn<Stock, String> nameCol = new TableColumn<>(controller.format("stock"));
        nameCol.setCellValueFactory(df -> df.getValue().nameProperty());
        tableView.getColumns().add(nameCol);

        Stream.iterate(4, x -> x - 1).limit(5).forEachOrdered(i -> {
            TableColumn<Stock, String> priceCol = new TableColumn<>();
            priceCol.setCellValueFactory(df -> market.getPrice(df.getValue(), i).asString("%.2f"));
            tableView.getColumns().add(priceCol);
        });

        tableView.setItems(market.getStocks());

        return tableView;
    }
}
