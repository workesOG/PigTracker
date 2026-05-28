// Theis Thomsen

package pigtracker.model;

public record MeanMedianMetric(String metric, double mean, double median, DisplayType displayType) {

    public String getMeanDisplayString() {
        return displayType.format(mean);
    }

    public String getMedianDisplayString() {
        return displayType.format(median);
    }
}
