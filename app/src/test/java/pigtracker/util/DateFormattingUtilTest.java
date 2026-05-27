// Af Nikolaj Jakobsen

package pigtracker.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateFormattingUtilTest {

    @Test
    void formatsSecondsAsMinutesAndSeconds() {
        assertEquals("02:03", DateFormattingUtil.formatSecondsAsMinSec(123));
    }

    @Test
    void padsSingleDigitMinutesAndSeconds() {
        assertEquals("00:05", DateFormattingUtil.formatSecondsAsMinSec(5));
    }

    @Test
    void formatsZeroSeconds() {
        assertEquals("00:00", DateFormattingUtil.formatSecondsAsMinSec(0));
    }

    @Test
    void roundsFractionalSecondsToNearestSecond() {
        // 122.6s rounds up to 123s -> 02:03.
        assertEquals("02:03", DateFormattingUtil.formatSecondsAsMinSec(122.6));
    }

    @Test
    void doesNotCapMinutesAtSixty() {
        // 3661s is 61 minutes and 1 second; minutes are not wrapped into hours.
        assertEquals("61:01", DateFormattingUtil.formatSecondsAsMinSec(3661));
    }
}
