package pigtracker.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateFormattingUtilTest {
    @Test
    void formatSecondsAsMinSecPadsMinutesAndSeconds() {
        assertEquals("02:03", DateFormattingUtil.formatSecondsAsMinSec(123));
    }

    @Test
    void formatSecondsAsMinSecRoundsToNearestSecond() {
        assertEquals("02:04", DateFormattingUtil.formatSecondsAsMinSec(123.5));
    }

    @Test
    void formatSecondsAsMinSecSupportsDurationsOverAnHour() {
        assertEquals("61:05", DateFormattingUtil.formatSecondsAsMinSec(3665));
    }

    @Test
    void dateFormattersUseExpectedPatterns() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 5, 26, 9, 7, 45);
        LocalDate date = LocalDate.of(2026, 5, 26);

        assertEquals("2026-05-26 09:07", dateTime.format(DateFormattingUtil.dateTimeFormatterNoSeconds));
        assertEquals("2026-05-26", date.format(DateFormattingUtil.dateFormatter));
    }
}
