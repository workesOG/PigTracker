// Af Nikolaj Jakobsen

package pigtracker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pigtracker.model.Visit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExportServiceTest {

    @Test
    void writeVisitsWritesHeaderAndRowsInImportFormat(@TempDir Path tempDir) throws IOException {
        List<Visit> visits = List.of(
                new Visit(0, 211863, "984000010111863", 0, 2, LocalDateTime.of(2026, 4, 8, 9, 14), 80, 95500, 5),
                new Visit(0, 211863, "984000010111863", 0, 2, LocalDateTime.of(2026, 4, 9, 10, 31), 53, null, 13));

        File out = tempDir.resolve("export.csv").toFile();
        int written = ExportService.writeVisits(out, visits);

        assertEquals(2, written);

        List<String> lines = Files.readAllLines(out.toPath());
        assertEquals(3, lines.size());
        assertEquals("animal_number;responder;location;visit_time;duration;weight;feed_intake", lines.get(0));
        assertEquals("211863;984000010111863;2;08/04/2026 09.14;80;95500;5", lines.get(1));
        // A null weight is written as an empty field, matching the PPT import format.
        assertEquals("211863;984000010111863;2;09/04/2026 10.31;53;;13", lines.get(2));
    }

    @Test
    void writeVisitsWritesOnlyHeaderForNoVisits(@TempDir Path tempDir) throws IOException {
        File out = tempDir.resolve("empty.csv").toFile();
        int written = ExportService.writeVisits(out, List.of());

        assertEquals(0, written);
        List<String> lines = Files.readAllLines(out.toPath());
        assertEquals(1, lines.size());
        assertEquals("animal_number;responder;location;visit_time;duration;weight;feed_intake", lines.get(0));
    }
}
