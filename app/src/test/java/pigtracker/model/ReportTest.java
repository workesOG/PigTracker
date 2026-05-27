// Af Nikolaj Jakobsen

package pigtracker.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReportTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 5, 28, 12, 0);

    private static Report sampleReport(int id) {
        return new Report(id, 1, LocalDateTime.of(2026, 4, 8, 9, 0), LocalDateTime.of(2026, 4, 9, 9, 0), 120, 30,
                Report.Status.COMPLETE, 2, CREATED_AT);
    }

    @Test
    void withIdReplacesOnlyTheId() {
        Report copy = sampleReport(0).withId(11);

        assertEquals(11, copy.id());
        assertEquals(1, copy.groupId());
        assertEquals(Report.Status.COMPLETE, copy.status());
        assertEquals(CREATED_AT, copy.createdAt());
    }

    @Test
    void errorReportUsesSentinelId() {
        // The error placeholder is identified by an id of -1.
        assertEquals(-1, Report.getReportListErrorReport().id());
    }

    @Test
    void toStringDescribesAnErrorReport() {
        assertEquals("Error fetching reports", Report.getReportListErrorReport().toString());
    }

    @Test
    void toStringFormatsReportNumberAndCreationTime() {
        // dateTimeFormatterNoSeconds renders as yyyy-MM-dd HH:mm.
        assertEquals("Report #11 - 2026-05-28 12:00", sampleReport(11).toString());
    }
}
