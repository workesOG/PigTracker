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
        File selectedFile = chooseCsvFile();
        if (selectedFile == null) {
            return;
        }

        String groupName = promptForGroupName();
        if (groupName == null || groupName.isBlank()) {
            return;
        }

        try {
            ImportService.importFromCSV(selectedFile, groupName, () -> {
                AppContext.getReportsController().refreshReportList();
            });
            showInfoAlert("Import Successful", "The CSV import was completed successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Import Failed", "An error occurred during the CSV import.", e.getMessage());
        }
    }

    // Theis Thomsen
    private File chooseCsvFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Visits from CSV");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        return fileChooser.showOpenDialog(Main.getPrimaryStage());
    }

    // Theis Thomsen
    private String promptForGroupName() {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Specify Group Name");
        dialog.setHeaderText("Enter a name for the new group:");
        dialog.setContentText("Group name:");

        java.util.Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            return result.get().trim();
        } else {
            return null;
        }
    }

    // Theis Thomsen
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Theis Thomsen
    private void showErrorAlert(String title, String header, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
