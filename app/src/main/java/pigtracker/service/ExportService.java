// Af Nikolaj Jakobsen

package pigtracker.service;

import pigtracker.dao.VisitDAO;
import pigtracker.model.Visit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ExportService {

    // Writes visits back out in the same format ImportService reads: animal_number;responder;location;visit_time;duration;weight;feed_intake
    private static final String HEADER = "animal_number;responder;location;visit_time;duration;weight;feed_intake";
    private static final String DELIMITER = ";";

    // Matches the date pattern ImportService parses (e.g. 08/04/2026 09.14).
    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH.mm",
            Locale.ENGLISH);

    // Writes the given visits to a CSV file in the PPT import format. Returns the number of visit rows written (excluding the header).
    public static int exportToCSV(File file, List<Visit> visits) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(HEADER);
            bw.newLine();

            for (Visit visit : visits) {
                bw.write(toRow(visit));
                bw.newLine();
            }
        }

        return visits.size();
    }

    // Exports every visit in the database.
    public static int exportAllToCSV(File file) throws IOException {
        try {
            return exportToCSV(file, VisitDAO.getAll());
        } catch (SQLException e) {
            throw new IOException("Failed to load visits for export", e);
        }
    }

    // Exports all visits belonging to one report (i.e. one prior import).
    public static int exportReportToCSV(File file, int reportId) throws IOException {
        try {
            return exportToCSV(file, VisitDAO.findByReportId(reportId));
        } catch (SQLException e) {
            throw new IOException("Failed to load report visits for export", e);
        }
    }

    // Builds one CSV line from a visit. A missing weight is written as an empty field, mirroring how the PPT machine omits weight when it could not weigh the pig during a visit.
    private static String toRow(Visit visit) {
        String weight = visit.weightG() == null ? "" : Integer.toString(visit.weightG());

        return String.join(DELIMITER, Integer.toString(visit.animalNumber()), visit.responder(),
                Integer.toString(visit.location()), DT_FORMATTER.format(visit.visitTime()),
                Integer.toString(visit.durationSec()), weight, Integer.toString(visit.feedIntakeG()));
    }
}
