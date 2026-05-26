// Theis Thomsen

package pigtracker.model;

import pigtracker.util.DateFormattingUtil;

public record MeanMedianMetric(String metric, double mean, double median, DisplayType displayType) {
    public String getMeanDisplayString() {
        return formatValue(mean);
    }

    public String getMedianDisplayString() {
        return formatValue(median);
    }

    private String formatValue(double value) {
        return switch (displayType) {
        case DECIMAL -> String.format("%.2f", value);
        case INT -> String.format("%d", (int)Math.round(value));
        case TIME -> DateFormattingUtil.formatSecondsAsMinSec(value);
        };
    }
}
