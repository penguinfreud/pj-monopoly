package monopoly.gui;

import javafx.scene.image.Image;

import java.util.Hashtable;
import java.util.Map;

public class ImageManager {
    private Map<String, Image> imageMap = new Hashtable<>();

    public Image getImage(String path) {
        if (imageMap.containsKey(path)) {
            return imageMap.get(path);
        } else {
            Image image = new Image(ImageManager.class.getResourceAsStream(path));
            imageMap.put(path, image);
            return image;
        }
    }
}
