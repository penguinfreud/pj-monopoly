package monopoly.gui.popups;

import javafx.beans.Observable;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import monopoly.Game;
import monopoly.IPlayer;
import monopoly.Shareholding;
import monopoly.gui.MainController;
import monopoly.stock.Stock;
import monopoly.stock.StockMarket;

public class StockWindow extends Stage {
    private MainController controller;
    private ObjectProperty<Stock> currentStock = new SimpleObjectProperty<>();
    private VBox root;
    private boolean inited = false;

    public StockWindow(MainController controller) {
        this.controller = controller;
        root = new VBox();
        Scene scene = new Scene(root, 400, 400);
        setScene(scene);
    }

    public void init() {
        if (!inited) {
            inited = true;
            root.getChildren().addAll(createTrendGraph(), createStockTable());
        }
    }

    private Node createTrendGraph() {
        Game g = controller.getGame();
        StockMarket market = StockMarket.getMarket(g);

        Axis<Number> xAxis = new NumberAxis(),
        yAxis = new NumberAxis();

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);

        currentStock.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                chart.setData(null);
            } else {
                ObservableList<XYChart.Data<Number, Number>> dataList = FXCollections.observableArrayList();
                for (int i = 0; i<10; i++) {
                    XYChart.Data<Number, Number> data = new XYChart.Data<>();
                    data.setXValue(10 - i);
                    data.YValueProperty().bind(market.getPrice(newValue, i));
                    dataList.add(data);
                }

                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                series.setName(newValue.toString(g));
                series.setData(dataList);
                chart.setData(FXCollections.singletonObservableList(series));
            }
        });

        return chart;
    }

    private Node createStockTable() {
        StockMarket market = StockMarket.getMarket(controller.getGame());

        TableView<Stock> tableView = new TableView<>();
        TableColumn<Stock, String> nameCol = new TableColumn<>(controller.format("stock"));
        nameCol.setCellValueFactory(df -> df.getValue().nameProperty());
        tableView.getColumns().add(nameCol);

        TableColumn<Stock, String> priceCol = new TableColumn<>(controller.getText("stock_price"));
        priceCol.setCellValueFactory(df -> market.getPrice(df.getValue(), 0).asString("%.2f"));
        tableView.getColumns().add(priceCol);

        TableColumn<Stock, String> fluctuationCol = new TableColumn<>(controller.getText("fluctuation"));
        fluctuationCol.setCellValueFactory(df -> {
            Stock stock = df.getValue();
            DoubleBinding prev = market.getPrice(stock, 1);
            return market.getPrice(stock, 0)
                    .subtract(prev)
                    .divide(prev)
                    .multiply(100)
                    .asString("%+.2f%%");
        });
        tableView.getColumns().add(fluctuationCol);

        Game g = controller.getGame();
        for (IPlayer player: g.getPlayers()) {
            TableColumn<Stock, Number> holdingCol = new TableColumn<>(player.getName() + controller.getText("holding"));
            holdingCol.setCellValueFactory(df -> Shareholding.get(player).getAmount(df.getValue()));
            tableView.getColumns().add(holdingCol);
        }

        TableColumn<Stock, String> averageCostCol = new TableColumn<>(controller.getText("average_cost"));
        g.currentPlayer().addListener((observable, oldValue, newValue) ->
                averageCostCol.setCellValueFactory(df -> Shareholding.get(newValue)
                        .getAverageCost(df.getValue()).asString("%.2f")));

        tableView.setItems(market.getStocks());

        tableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> currentStock.set(newValue));

        return tableView;
    }
}
