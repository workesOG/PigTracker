package pigtracker.controller;

import javafx.fxml.FXML;
import pigtracker.Main;
import pigtracker.model.Visit;
import pigtracker.service.UserService;
import pigtracker.util.Session;

import javafx.stage.FileChooser;
import pigtracker.service.ImportService;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
            int reportId = 1; // For testing, change later.

            try {
                List<Visit> importedVisits = ImportService.importFromCSV(selectedFile, reportId);
                System.out.println("Imported " + importedVisits.size() + " visits.");
            } catch (IOException e) {
                e.printStackTrace();
                // TODO: Show alert dialogue
            }
        }
    }
}
