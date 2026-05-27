// Theis Thomsen

package pigtracker.service;

import pigtracker.dao.GroupDAO;
import pigtracker.dao.ReportDAO;
import java.sql.SQLException;

public class DatabaseCleanupService {
    public static void cleanupInProgressGroupsAndReports() throws SQLException {
        ReportDAO.deleteAllInProgress();
        GroupDAO.deleteAllInProgress();
    }
}