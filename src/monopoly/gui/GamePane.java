package monopoly.gui;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import monopoly.BasePlayer;
import monopoly.Cards;
import monopoly.Game;
import monopoly.IPlayer;

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

        return text;
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
        Button useCardBtn = controller.createButton("use_card",
                e -> ((GUIPlayer) controller.getGame().getCurrentPlayer()).useCard());
        rightPane.getChildren().addAll(
                new HBox(useCardBtn),
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
