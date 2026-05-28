// Theis Thomsen

package pigtracker.service;

import java.sql.SQLException;

import pigtracker.dao.GroupDAO;
import pigtracker.dao.ReportDAO;

public final class DatabaseCleanupService {

    private DatabaseCleanupService() {}

    public static void cleanupInProgressGroupsAndReports() throws SQLException {
        ReportDAO.deleteAllInProgress();
        GroupDAO.deleteAllInProgress();
    }
}
