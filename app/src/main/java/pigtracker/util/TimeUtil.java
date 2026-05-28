// Theis Thomsen

package pigtracker.util;

import java.time.LocalDateTime;

public final class TimeUtil {

    private TimeUtil() {}

    public static LocalDateTime minusPeriod(LocalDateTime from, String period) {
        return switch (period) {
            case "7 Days" -> from.minusDays(7);
            case "1 Month" -> from.minusMonths(1);
            case "6 Months" -> from.minusMonths(6);
            default -> throw new IllegalArgumentException("Unsupported period: " + period);
        };
    }
}
