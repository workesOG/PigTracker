// Theis Thomsen

package pigtracker.util;

import pigtracker.controller.ReportsController;

public class AppContext {
    private static ReportsController reportsController;

    public static ReportsController getReportsController() {
        return reportsController;
    };

    public static void setReportsController(ReportsController controller) {
        reportsController = controller;
    }
}
