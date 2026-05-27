// Theis Thomsen

package pigtracker.model;

import java.util.List;

public record HistoricalImportComparisonMetrics(List<String> periods, List<Double> values) {}