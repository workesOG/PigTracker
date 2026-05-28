package pigtracker.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import pigtracker.Main;
import pigtracker.dao.GroupDAO;
import pigtracker.dao.ReportDAO;
import pigtracker.dao.VisitDAO;
import pigtracker.model.Group;
import pigtracker.model.Report;
import pigtracker.service.UserService;
import pigtracker.util.AppContext;
import pigtracker.util.Session;

import javafx.stage.FileChooser;
import pigtracker.service.ImportService;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TabPane;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import javafx.scene.control.MenuItem;

public class MainController {
    @FXML
    private TabPane mainTabPane;

    @FXML
    private MenuItem toggleDiscontinuedAnimalsMenu;

    private boolean showDiscontinuedAnimals = false;

    public boolean isShowDiscontinuedAnimals() {
        return showDiscontinuedAnimals;
    }

    public void setShowDiscontinuedAnimals(boolean value) {
        showDiscontinuedAnimals = value;
    }

    // Theis Thomsen
    @FXML
    public void initialize() {
        AppContext.setMainController(this);
        updateMenuText();
        // Listen for tab changes, used for clearing report window if you switch away
        mainTabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            if (newIdx == null || newIdx.intValue() != 2) {
                var reportsController = AppContext.getReportsController();
                if (reportsController != null) {
                    reportsController.clearReportView();
                }
            }
        });
    }

    @FXML
    private void toggleShowDiscontinuedAnimals() {
        showDiscontinuedAnimals = !showDiscontinuedAnimals;
        updateMenuText();
        // Refresh DataController's animal table if needed
        DataController dataController = AppContext.getDataController();
        if (dataController != null) {
            dataController.loadAnimalData();
        }
    }

    private void updateMenuText() {
        if (toggleDiscontinuedAnimalsMenu != null) {
            toggleDiscontinuedAnimalsMenu
                    .setText(showDiscontinuedAnimals ? "Hide Discontinued Animals" : "Show Discontinued Animals");
        }
    }

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
    private void handleImport() {
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
    @FXML
    public void handleReimport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select new CSV file");
        File file = fileChooser.showOpenDialog(Main.getPrimaryStage());
        if (file == null)
            return;

        List<Report> reports;
        try {
            reports = ReportDAO.getAllCompleted();
        } catch (SQLException e) {
            showErrorAlert("Re-Import Failed", "Failed to get reports", e.getMessage());
            e.printStackTrace();
            return;
        }
        ObservableList<String> options = FXCollections.observableArrayList();
        for (Report r : reports) {
            options.add(r.toString());
        }

        String selected = MenuSelectionWindowController.showAndWait(Main.getPrimaryStage(), "report", options);
        if (selected == null)
            return;

        Report oldReport = reports.stream().filter(r -> r.toString().equals(selected)).findFirst().orElse(null);
        if (oldReport == null)
            return;

        int oldGroupId = oldReport.groupId();
        LocalDateTime originalCreatedAt = oldReport.createdAt();
        String groupName;

        try {
            groupName = GroupDAO.findById(oldGroupId).orElse(null).name();
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Re-Import Failed", "Failed to find groups by ID", e.getMessage());
            return;
        }

        try {
            VisitDAO.deleteByReportId(oldReport.id());
            ReportDAO.delete(oldReport.id());
        } catch (SQLException e) {
            showErrorAlert("Re-Import Failed", "Failed to delete old report data", e.getMessage());
            e.printStackTrace();
            return;
        }

        try {
            ImportService.importFromCSV(file, groupName, originalCreatedAt);
        } catch (IOException e) {
            showErrorAlert("Re-Import Failed", "Failed to import new data", e.getMessage());
            e.printStackTrace();
        }

        AppContext.getReportsController().refreshReportList();
    }

    // Theis Thomsen
    @FXML
    private void handleExport() {
        List<Group> groups;
        try {
            groups = pigtracker.dao.GroupDAO.getAll();
        } catch (SQLException e) {
            showErrorAlert("Export Failed", "Failed to load groups", e.getMessage());
            return;
        }

        if (groups.isEmpty()) {
            showErrorAlert("Export Failed", "No groups found", "At least one group is required to export data.");
            return;
        }

        ObservableList<String> options = FXCollections.observableArrayList();
        for (var group : groups) {
            options.add(group.name());
        }

        String selectedGroupName = MenuSelectionWindowController.showAndWait(Main.getPrimaryStage(), "group", options);
        if (selectedGroupName == null)
            return;

        Group selectedGroup = groups.stream().filter(g -> g.name().equals(selectedGroupName))
                .findFirst().orElse(null);

        if (selectedGroup == null) {
            showErrorAlert("Export Failed", "Group not found", "Unable to locate the selected group.");
            return;
        }

        List<Report> reports;
        try {
            reports = ReportDAO.findCompletedByGroupId(selectedGroup.id());
        } catch (SQLException e) {
            showErrorAlert("Export Failed", "Failed to load reports for group", e.getMessage());
            return;
        }

        if (reports.isEmpty()) {
            showErrorAlert("Export Failed", "No Reports", "No completed reports exist for the selected group.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export visits to CSV");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showSaveDialog(Main.getPrimaryStage());
        if (file == null)
            return;

        try {
            int nRows = pigtracker.service.ExportService.exportToCSV(file, reports);
            showInfoAlert("Export Successful",
                    "Successfully exported " + nRows + " visits to:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            showErrorAlert("Export Failed", "IO Error", e.getMessage());
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
