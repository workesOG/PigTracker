// Theis Thomsen

package pigtracker.model;

import pigtracker.util.DateFormattingUtil;

public enum DisplayType {
    DECIMAL, INT, TIME;

    public String format(double value) {
        return switch (this) {
            case DECIMAL -> String.format(DateFormattingUtil.APP_LOCALE, "%.2f", value);
            case INT -> Integer.toString((int) Math.round(value));
            case TIME -> DateFormattingUtil.formatSecondsAsMinSec(value);
        };
    }
}
