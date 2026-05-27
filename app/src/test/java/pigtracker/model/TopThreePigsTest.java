// Af Nikolaj Jakobsen

package pigtracker.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class TopThreePigsTest {

    @Test
    void decimalDisplayFormatsEachValueToTwoDecimals() {
        TopThreePigs top = new TopThreePigs("FCR", new int[] { 1, 2 }, new double[] { 1.5, 2.345 },
                DisplayType.DECIMAL);

        assertArrayEquals(new String[] { "1.50", "2.35" }, top.getDisplayStrings());
    }

    @Test
    void intDisplayRoundsEachValueToWholeNumber() {
        TopThreePigs top = new TopThreePigs("Most Visits", new int[] { 1, 2 }, new double[] { 3.0, 2.4 },
                DisplayType.INT);

        assertArrayEquals(new String[] { "3", "2" }, top.getDisplayStrings());
    }

    @Test
    void timeDisplayFormatsEachValueAsMinutesSeconds() {
        TopThreePigs top = new TopThreePigs("Longest Visit", new int[] { 1, 2 }, new double[] { 123.0, 90.0 },
                DisplayType.TIME);

        assertArrayEquals(new String[] { "02:03", "01:30" }, top.getDisplayStrings());
    }
}
