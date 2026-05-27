// Af Nikolaj Jakobsen

package pigtracker.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MeanMedianMetricTest {

    @Test
    void decimalDisplayFormatsToTwoDecimals() {
        MeanMedianMetric metric = new MeanMedianMetric("FCR", 1.5, 2.345, DisplayType.DECIMAL);

        assertEquals("1.50", metric.getMeanDisplayString());
        assertEquals("2.35", metric.getMedianDisplayString()); // rounded to two decimals
    }

    @Test
    void intDisplayRoundsToWholeNumber() {
        MeanMedianMetric metric = new MeanMedianMetric("Visits", 2.6, 4.0, DisplayType.INT);

        assertEquals("3", metric.getMeanDisplayString()); // 2.6 rounds up to 3
        assertEquals("4", metric.getMedianDisplayString());
    }

    @Test
    void timeDisplayFormatsAsMinutesSeconds() {
        MeanMedianMetric metric = new MeanMedianMetric("Visit Duration (s)", 123.0, 90.0, DisplayType.TIME);

        assertEquals("02:03", metric.getMeanDisplayString());
        assertEquals("01:30", metric.getMedianDisplayString());
    }
}
