// Theis Thomsen
package pigtracker.service;

import pigtracker.dao.ReportDAO;
import pigtracker.model.Report;
import pigtracker.model.Visit;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ReportImportService {
    public static void generateReportData(int reportId) {
        // TODO: Implement logic to load visit data, compute metrics, top pigs, etc.
        // This method will be called when showing a report in the report window
    }

    public static int createReport(LocalDateTime importStart, LocalDateTime importEnd, int rowCount, int pigCount,
            int createdBy) throws SQLException {
        Report report = new Report(0, importStart, importEnd, rowCount, pigCount, null, createdBy, null);
        Report created = ReportDAO.create(report);
        return created.id();
    }
}
