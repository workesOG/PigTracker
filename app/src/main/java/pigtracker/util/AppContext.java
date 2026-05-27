// Theis Thomsen

package pigtracker.util;

import pigtracker.controller.DashboardController;
import pigtracker.controller.ReportsController;

public class AppContext {
    private static ReportsController reportsController;
    private static DashboardController dashboardController;

    public static ReportsController getReportsController() {
        return reportsController;
    };

    public static void setReportsController(ReportsController controller) {
        reportsController = controller;
    }

    public static DashboardController getDashboardController() {
        return dashboardController;
    }

    public static void setDashboardController(DashboardController controller) {
        dashboardController = controller;
    }
}
