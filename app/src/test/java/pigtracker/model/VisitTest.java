// Af Nikolaj Jakobsen

package pigtracker.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class VisitTest {

    private static final LocalDateTime VISIT_TIME = LocalDateTime.of(2026, 4, 8, 9, 14);

    @Test
    void withIdReplacesOnlyTheId() {
        Visit original = new Visit(0, 211863, "984000010111863", 7, 2, VISIT_TIME, 80, 95500, 5);

        Visit copy = original.withId(42);

        assertEquals(42, copy.id());
        // Every other field is carried over unchanged.
        assertEquals(211863, copy.animalNumber());
        assertEquals("984000010111863", copy.responder());
        assertEquals(7, copy.reportId());
        assertEquals(2, copy.location());
        assertEquals(VISIT_TIME, copy.visitTime());
        assertEquals(80, copy.durationSec());
        assertEquals(95500, copy.weightG());
        assertEquals(5, copy.feedIntakeG());
    }

    @Test
    void withReportIdReplacesOnlyTheReportId() {
        Visit original = new Visit(3, 211863, "984000010111863", -1, 2, VISIT_TIME, 80, 95500, 5);

        Visit copy = original.withReportId(9);

        assertEquals(9, copy.reportId());
        // The id and remaining fields are preserved.
        assertEquals(3, copy.id());
        assertEquals(211863, copy.animalNumber());
    }

    @Test
    void withReportIdPreservesAMissingWeight() {
        // A null weight (PPT machine did not weigh the pig) must survive the copy.
        Visit original = new Visit(0, 211863, "984000010111863", -1, 2, VISIT_TIME, 80, null, 5);

        assertNull(original.withReportId(9).weightG());
    }
}
