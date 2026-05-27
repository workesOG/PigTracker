// Theis Thomsen

package pigtracker.util;

import java.time.LocalDateTime;

public class TimeUtil {
    public static LocalDateTime minusPeriod(LocalDateTime from, String period) {
        switch (period) {
            case "7 Days":
                return from.minusDays(7);
            case "1 Month":
                return from.minusMonths(1);
            case "6 Months":
                return from.minusMonths(6);
            default:
                throw new IllegalArgumentException("Unsupported period: " + period);
        }
    }
}