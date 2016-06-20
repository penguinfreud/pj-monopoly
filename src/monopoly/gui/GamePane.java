package monopoly.gui;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import monopoly.BasePlayer;
import monopoly.Cards;
import monopoly.Game;
import monopoly.IPlayer;
import monopoly.place.Place;

public class GamePane implements IPane {
    private MainController controller;

    private Group mapView = new Group();
    private VBox rightPane = new VBox();

    public GamePane(MainController controller) {
        this.controller = controller;

        createRightPane();
    }

    @Override
    public Node center() {
        return mapView;
    }

    @Override
    public Node top() {
        Text text = new Text();
        Game g = controller.getGame();
        g.onGameOver.addListener(winner ->
                text.setText(g.format("game_over", g.getCurrentPlayer().getName())));
        g.onLanded.addListener(() ->
                text.setText(g.format("you_have_arrived",
                        g.getCurrentPlayer().getCurrentPlace().toString(g))));
        g.onBankrupt.addListener((p) ->
                text.setText(g.format("bankrupt", p.getName())));
        g.onException.addListener(text::setText);

        BasePlayer.onMoneyChange.get(g).addListener((player, amount, msg) -> {
            if (!msg.isEmpty()) {
                text.setText(msg);
            }
        });
        Cards.onCouponChange.get(g).addListener((player, amount) -> {
            if (amount > 0) {
                text.setText(g.format("get_coupons", player.getName(), amount));
            }
        });
        Cards.onCardChange.get(g).addListener((player, isGet, card) -> {
            if (isGet) {
                text.setText(g.format("get_card", player.getName(), card.toString(g)));
            }
        });

        Button useCardBtn = controller.createButton("use_card",
                e -> ((GUIPlayer) g.getCurrentPlayer()).useCard());
        Button playerInfoBtn = controller.createButton("view_player_info",
                e -> controller.togglePlayerInfoWindow());
        Button checkAlertBtn = controller.createButton("check_alert",
                e -> {
                    Place place = g.getCurrentPlayer().getCurrentPlace();
                    boolean hasRoadblock = false;
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle(controller.getText("check_alert"));
                    alert.setHeaderText(controller.getText("check_alert"));
                    for (int i = 0; i < 10; i++) {
                        place = place.getNext();
                        if (place.hasRoadblock()) {
                            hasRoadblock = true;
                            alert.setContentText(g.format("has_roadblock", i + 1));
                        }
                    }
                    if (!hasRoadblock) {
                        alert.setContentText(g.getText("has_no_roadblock"));
                    }
                    alert.showAndWait();
                });
        Button tradeStockBtn = controller.createButton("trade_stock",
                e -> controller.toggleStockWindow());
        Button buyLotteryBtn = controller.createButton("buy_lottery",
                e -> {

                });
        Button giveUpBtn = controller.createButton("give_up",
                e -> g.getCurrentPlayer().giveUp());

        return new VBox(
                new HBox(useCardBtn, playerInfoBtn, checkAlertBtn, tradeStockBtn, giveUpBtn),
                text);
    }

    @Override
    public Node right() {
        return rightPane;
    }

    @Override
    public void onShow() {
        mapView.getChildren().addAll(controller.getMap().createMapView());
        for (IPlayer player : controller.getGame().getPlayers()) {
            mapView.getChildren().add(GUIPlayerInfo.get(player).getToken());
        }
    }

    private void createRightPane() {
        rightPane.getChildren().addAll(
                CurrentPlayerInfoPane.get(controller),
                DiceView.get(controller));
        rightPane.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                new BorderWidths(0, 0, 0, 1))));
        rightPane.setSpacing(10);
        rightPane.setPadding(new Insets(10));
    }
}
