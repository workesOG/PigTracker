// Af Nikolaj Jakobsen

package pigtracker.model;

import java.time.LocalDateTime;

public record DbReport(int id, LocalDateTime importStart, LocalDateTime importEnd, int rowCount, int pigCount, int createdBy, LocalDateTime createdAt) {

    // Returns a copy of this report with the given id (used after insert).
    public DbReport withId(int newId) {
        return new DbReport(newId, importStart, importEnd, rowCount, pigCount, createdBy, createdAt);
    }
}
