// Theis Thomsen

package pigtracker.model;

public record DashboardMetrics(double avgWeight, double avgFeed, double avgFcr, double weightChangePct,
        double feedChangePct, double fcrChangePct, int population, double populationChangePct) {}
