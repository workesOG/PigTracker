// Theis Thomsen

package pigtracker.model;

import java.util.List;

public record ReportMetrics(List<MeanMedianMetric> meanMedian, List<TopThreePigs> topThree) {}
