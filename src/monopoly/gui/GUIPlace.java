package monopoly.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;
import monopoly.place.*;

import java.util.Hashtable;
import java.util.Map;

public class GUIPlace extends DelegatePlace {
    private static final Map<Class<? extends Place>, String> placeIconMap = new Hashtable<>();

    public static void registerIcon(Class<? extends Place> theClass, String token) {
        placeIconMap.put(theClass, token);
    }

    static {
        MainController.putDefaultConfig("place-token-size", 48);
        registerIcon(News.class, "news.png");
        registerIcon(CardSite.class, "card_site.png");
        registerIcon(CardShop.class, "card_shop.png");
        registerIcon(CouponSite.class, "coupon_site.png");
        GameMap.registerPlaceReader("GUIPlace", (r, sc) -> new GUIPlace(
                ((GUIGameMap.GUIGameMapReader) r).getController(),
                sc.nextDouble(), sc.nextDouble(), r.readPlace(sc)));
    }

    private MainController controller;
    private DoubleProperty x = new SimpleDoubleProperty(),
    y = new SimpleDoubleProperty();
    private ObjectProperty<String> icon = new SimpleObjectProperty<>();
    private Shape token;

    public GUIPlace(MainController controller, double x, double y, Place place) {
        super(place);
        this.controller = controller;
        this.x.set(x);
        this.y.set(y);
        icon.set(placeIconMap.get(place.getClass()));
    }

    public Shape getToken() {
        if (token == null) {
            token = new Rectangle(64, 64);
            token.fillProperty().bind(Bindings.createObjectBinding(() ->
                    icon.get() == null? null:
                            new ImagePattern(controller.getImageManager().getImage("/icons/places/" + icon.get())), icon));
            token.translateXProperty().bind(x);
            token.translateYProperty().bind(y);
            token.setStroke(Color.BLACK);
            token.setStrokeWidth(1);
            token.setStrokeType(StrokeType.OUTSIDE);
        }
        return token;
    }
}
