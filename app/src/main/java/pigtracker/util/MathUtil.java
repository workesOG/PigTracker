package pigtracker.util;

import java.util.ArrayList;
import java.util.List;

public class MathUtil {
    public static double mean(List<Double> values) {
        if (values.isEmpty())
            return 0;
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    public static double median(List<Double> values) {
        if (values.isEmpty())
            return 0;
        List<Double> sorted = new ArrayList<>(values);
        java.util.Collections.sort(sorted);
        int n = sorted.size();
        return n % 2 == 1 ? sorted.get(n / 2) : (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;
    }
}
