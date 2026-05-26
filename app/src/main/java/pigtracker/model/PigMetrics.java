// Theis Thomsen

package pigtracker.model;

public record PigMetrics(int animalNumber, double feedPerVisit, double feedPerDay, double avgVisitDuration,
        int visitCount, double latestWeight, double fcr, double longestVisitDuration) {}