// Af Nikolaj Jakobsen

package pigtracker.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimeUtilTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2026, 5, 28, 12, 0);

    @Test
    void subtractsSevenDays() {
        assertEquals(LocalDateTime.of(2026, 5, 21, 12, 0), TimeUtil.minusPeriod(BASE, "7 Days"));
    }

    @Test
    void subtractsOneMonth() {
        assertEquals(LocalDateTime.of(2026, 4, 28, 12, 0), TimeUtil.minusPeriod(BASE, "1 Month"));
    }

    @Test
    void subtractsSixMonths() {
        assertEquals(LocalDateTime.of(2025, 11, 28, 12, 0), TimeUtil.minusPeriod(BASE, "6 Months"));
    }

    @Test
    void throwsForUnsupportedPeriod() {
        assertThrows(IllegalArgumentException.class, () -> TimeUtil.minusPeriod(BASE, "1 Year"));
    }
}
