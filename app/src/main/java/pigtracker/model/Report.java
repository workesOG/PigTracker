// Af Nikolaj Jakobsen

package pigtracker.model;

import java.time.LocalDateTime;

public record Report(int id, LocalDateTime importStart, LocalDateTime importEnd, int rowCount, int pigCount,
        Status status, int createdBy, LocalDateTime createdAt) {

    public enum Status {
        IN_PROGRESS, COMPLETE
    }

    // Returns a copy of this report with the given id (used after insert).
    public Report withId(int newId) {
        return new Report(newId, importStart, importEnd, rowCount, pigCount, status, createdBy, createdAt);
    }

    public static Report getReportListErrorReport() {
        return new Report(-1, null, null, -1, -1, Report.Status.IN_PROGRESS, -1, null);
    }

    @Override
    public String toString() {
        if (id == -1) {
            return "Error fetching reports";
        }
        return "Report #" + id + " - " + createdAt.toString();
    }
}
