// Theis Thomsen

package pigtracker.util;

import java.util.List;

public final class MathUtil {

    private MathUtil() {}

    public static double mean(List<Double> values) {
        if (values.isEmpty())
            return 0;
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    public static double median(List<Double> values) {
        if (values.isEmpty())
            return 0;
        double[] sorted = values.stream().mapToDouble(Double::doubleValue).sorted().toArray();
        int n = sorted.length;
        return n % 2 == 1
                ? sorted[n / 2]
                : (sorted[n / 2 - 1] + sorted[n / 2]) / 2.0;
    }

    // Uses |oldValue| as the denominator so a negative baseline still yields a correctly signed change.
    public static double percentChange(double oldValue, double newValue) {
        if (oldValue == 0)
            return 0.0;
        return ((newValue - oldValue) / Math.abs(oldValue)) * 100.0;
    }
}
