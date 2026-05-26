// Theis Thomsen

package pigtracker.util;

import java.time.format.DateTimeFormatter;

public class DateFormattingUtil {
    public static DateTimeFormatter dateTimeFormatterNoSeconds = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Formats a duration in seconds as MM:ss (e.g., 123 -> "02:03")
    public static String formatSecondsAsMinSec(double seconds) {
        int totalSeconds = (int)Math.round(seconds);
        int minutes = totalSeconds / 60;
        int secs = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
}
