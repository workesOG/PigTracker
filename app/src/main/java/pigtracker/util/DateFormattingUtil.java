// Theis Thomsen

package pigtracker.util;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class DateFormattingUtil {

    public static final Locale APP_LOCALE = Locale.forLanguageTag("da-DK");

    public static final DateTimeFormatter dateTimeFormatterNoSeconds = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DateFormattingUtil() {}

    // Formats a duration in seconds as MM:ss (e.g., 123 -> "02:03"). Minutes are not wrapped into hours.
    public static String formatSecondsAsMinSec(double seconds) {
        int totalSeconds = (int) Math.round(seconds);
        int minutes = totalSeconds / 60;
        int secs = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
}
