// Af Nikolaj Jakobsen

package pigtracker.service;

import pigtracker.dao.VisitDAO;
import pigtracker.model.Report;
import pigtracker.model.Visit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExportService {

    // Writes visits back out in the same format ImportService reads: animal_number;responder;location;visit_time;duration;weight;feed_intake
    private static final String HEADER = "animal_number;responder;location;visit_time;duration;weight;feed_intake";
    private static final String DELIMITER = ";";

    // Matches the date pattern ImportService parses (e.g. 08/04/2026 09.14).
    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH.mm",
            Locale.ENGLISH);

    // Builds the visit list from every visit in the given reports and writes it to a CSV file in the PPT import format. Returns the number of visit rows written (excluding the header).
    public static int exportToCSV(File file, List<Report> reports) throws IOException {
        return writeVisits(file, collectVisits(reports));
    }

    // Gathers every visit belonging to the given reports, oldest first within each report.
    private static List<Visit> collectVisits(List<Report> reports) throws IOException {
        List<Visit> visits = new ArrayList<>();

        try {
            for (Report report : reports) {
                visits.addAll(VisitDAO.findByReportId(report.id()));
            }
        } catch (SQLException e) {
            throw new IOException("Failed to load report visits for export", e);
        }

        return visits;
    }

    // Writes the given visits to a CSV file in the PPT import format. Returns the number of visit rows written (excluding the header).
    static int writeVisits(File file, List<Visit> visits) throws IOException {
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

    // Builds one CSV line from a visit. A missing weight is written as an empty field, mirroring how the PPT machine omits weight when it could not weigh the pig during a visit.
    private static String toRow(Visit visit) {
        String weight = visit.weightG() == null ? "" : Integer.toString(visit.weightG());

        return String.join(DELIMITER, Integer.toString(visit.animalNumber()), visit.responder(),
                Integer.toString(visit.location()), DT_FORMATTER.format(visit.visitTime()),
                Integer.toString(visit.durationSec()), weight, Integer.toString(visit.feedIntakeG()));
    }
}
