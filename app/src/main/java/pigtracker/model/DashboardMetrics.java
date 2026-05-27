// Theis Thomsen

package pigtracker.model;

public record DashboardMetrics(KpiMetrics weight, KpiMetrics feed, KpiMetrics fcr, KpiMetrics population,
                PopulationDistributionGraphMetrics populationDistribution,
                HistoricalImportComparisonMetrics historicalImportComparison) {}
