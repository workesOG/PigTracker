// Af Theis Thomsen

package pigtracker;

import java.sql.Connection;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pigtracker.dao.ConnectionDAO;
import pigtracker.service.DatabaseCleanupService;
import pigtracker.service.UserService;
import pigtracker.util.AppContext;

public class Main extends Application {

    private static Stage primaryStage;

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        primaryStage.setResizable(false);

        if (hasSavedSession()) {
            showMainView();
        } else {
            showLoginView();
        }
        primaryStage.show();

        verifyDatabaseConnection();
        cleanUpInProgressData();
    }

    public static void onDashboardReady() throws Exception {
        AppContext.getDashboardController().setDashboardMetrics();
    }

    public static void showLoginView() throws Exception {
        showScene("/views/login-view.fxml", "Login");
    }

    public static void showMainView() throws Exception {
        showScene("/views/main-view.fxml", "PPT Manager Interface");
    }

    private static void showScene(String fxmlPath, String title) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlPath));
        primaryStage.setScene(new Scene(loader.load()));
        primaryStage.setTitle(title);
    }

    // Af Nikolaj Jakobsen
    private boolean hasSavedSession() {
        try {
            return UserService.restoreSession();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void verifyDatabaseConnection() {
        try (Connection conn = ConnectionDAO.getConnection()) {
            System.out.println("Connected. Database: " + conn.getCatalog());
        } catch (Exception e) {
            System.out.println("FAILED to connect:");
            e.printStackTrace();
        }
    }

    private static void cleanUpInProgressData() {
        try {
            DatabaseCleanupService.cleanupInProgressGroupsAndReports();
        } catch (SQLException e) {
            System.out.println("FAILED to clean up database:");
            e.printStackTrace();
        }
    }
}
