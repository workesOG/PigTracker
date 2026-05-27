// Theis Thomsen

package pigtracker.model;

import java.util.List;

public record KpiMetrics(String title, double value, int decimals, String unit, double trend, String description,
        List<Double> history) {}
