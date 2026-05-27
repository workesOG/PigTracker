package pigtracker.controller;

import javafx.fxml.FXML;
import pigtracker.Main;
import pigtracker.service.UserService;
import pigtracker.util.AppContext;
import pigtracker.util.Session;

import javafx.stage.FileChooser;
import pigtracker.service.ImportService;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.File;
import java.io.IOException;

public class MainController {
    // Nikolaj Jakobsen
    @FXML
    private void handleLogout() {
        UserService.logout();

        try {
            Main.showLoginView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Theis Thomsen
    @FXML
    private void handleClearSessionData() {
        Session.clear();
    }

    // Theis Thomsen
    @FXML
    private void openImportWindow() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Visits from CSV");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File selectedFile = fileChooser.showOpenDialog(Main.getPrimaryStage());

        if (selectedFile != null) {
            try {
                ImportService.importFromCSV(selectedFile, () -> {
                    AppContext.getReportsController().refreshReportList();
                });
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Import Successful");
                alert.setHeaderText(null);
                alert.setContentText("The CSV import was completed successfully.");
                alert.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Import Failed");
                alert.setHeaderText("An error occurred during the CSV import.");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }
}
