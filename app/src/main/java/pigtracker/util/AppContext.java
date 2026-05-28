// Theis Thomsen

package pigtracker.util;

import pigtracker.controller.DashboardController;
import pigtracker.controller.DataController;
import pigtracker.controller.MainController;
import pigtracker.controller.ReportsController;

public final class AppContext {

    private static ReportsController reportsController;
    private static DashboardController dashboardController;
    private static DataController dataController;
    private static MainController mainController;

    private AppContext() {}

    public static ReportsController getReportsController() {
        return reportsController;
    }

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
    }

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
