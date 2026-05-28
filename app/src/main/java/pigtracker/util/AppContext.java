// Theis Thomsen

package pigtracker.util;

import pigtracker.controller.DashboardController;
import pigtracker.controller.DataController;
import pigtracker.controller.ReportsController;
import pigtracker.controller.MainController;

public class AppContext {
    private static ReportsController reportsController;
    private static DashboardController dashboardController;
    private static DataController dataController;
    private static MainController mainController;

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

    public static DataController getDataController() {
        return dataController;
    };

    public static void setDataController(DataController controller) {
        dataController = controller;
    }

    public static MainController getMainController() {
        return mainController;
    }

    public static void setMainController(MainController controller) {
        mainController = controller;
    }
}
