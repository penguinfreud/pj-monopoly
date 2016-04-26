package monopoly.util;

import java.text.DecimalFormat;

public class Util {
    private static final DecimalFormat df = new DecimalFormat("#,##0.00");

    public static final String formatNumber(double val) {
        if (Double.isNaN(val)) {
            return "n/a";
        } else {
            return df.format(val);
        }
    }
}
