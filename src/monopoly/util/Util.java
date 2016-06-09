package monopoly.util;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Util {
    private static final DecimalFormat df = new DecimalFormat("#,##0.00");

    public static final String formatNumber(double val) {
        if (Double.isNaN(val)) {
            return "n/a";
        } else {
            return df.format(val);
        }
    }

    public static String getText(ResourceBundle messages, String key) {
        try {
            return new String(messages.getString(key).getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        } catch (MissingResourceException e) {
            //logger.log(Level.INFO, "Unknown key: " + key);
            return key;
        }

    }
}
