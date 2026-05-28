package pigtracker.controller;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import pigtracker.Main;
import pigtracker.dao.GroupDAO;
import pigtracker.dao.ReportDAO;
import pigtracker.dao.VisitDAO;
import pigtracker.model.Group;
import pigtracker.model.Report;
import pigtracker.model.User;
import pigtracker.service.ExportService;
import pigtracker.service.ImportService;
import pigtracker.service.UserService;
import pigtracker.util.Alerts;
import pigtracker.util.AppContext;
import pigtracker.util.Session;

public class MainController {

    private static final int REPORTS_TAB_INDEX = 2;
    private static final FileChooser.ExtensionFilter CSV_FILTER = new FileChooser.ExtensionFilter("CSV Files", "*.csv");

    @FXML
    private TabPane mainTabPane;

    @FXML
    private Menu importExportMenu;

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
        updateImportExportMenuVisibility();
        updateMenuText();
        mainTabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            if (newIdx == null || newIdx.intValue() != REPORTS_TAB_INDEX) {
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
        DataController dataController = AppContext.getDataController();
        if (dataController != null) {
            dataController.loadAnimalData();
        }
    }

    private void updateImportExportMenuVisibility() {
        boolean hasAdminPermissions = Session.getCurrentUser().permission() == User.Permission.ADMIN;
        importExportMenu.setDisable(!false);
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
        File selectedFile = chooseCsvFile("Import Visits from CSV");
        if (selectedFile == null)
            return;

        String groupName = promptForGroupName();
        if (groupName == null || groupName.isBlank())
            return;

        try {
            ImportService.importFromCSV(selectedFile, groupName,
                    () -> AppContext.getReportsController().refreshReportList());
            Alerts.info("Import Successful", "The CSV import was completed successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            Alerts.error("Import Failed", "An error occurred during the CSV import.", e.getMessage());
        }
    }

    // Theis Thomsen
    @FXML
    public void handleReimport() {
        File file = chooseCsvFile("Select new CSV file");
        if (file == null)
            return;

        List<Report> reports;
        try {
            reports = ReportDAO.getAllCompleted();
        } catch (SQLException e) {
            Alerts.error("Re-Import Failed", "Failed to get reports", e.getMessage());
            e.printStackTrace();
            return;
        }

        Report oldReport = pickReport(reports);
        if (oldReport == null)
            return;

        String groupName;
        try {
            groupName = GroupDAO.findById(oldReport.groupId()).map(Group::name).orElse(null);
        } catch (SQLException e) {
            e.printStackTrace();
            Alerts.error("Re-Import Failed", "Failed to find groups by ID", e.getMessage());
            return;
        }

        try {
            VisitDAO.deleteByReportId(oldReport.id());
            ReportDAO.delete(oldReport.id());
        } catch (SQLException e) {
            Alerts.error("Re-Import Failed", "Failed to delete old report data", e.getMessage());
            e.printStackTrace();
            return;
        }

        LocalDateTime originalCreatedAt = oldReport.createdAt();
        try {
            ImportService.importFromCSV(file, groupName, originalCreatedAt);
        } catch (IOException e) {
            Alerts.error("Re-Import Failed", "Failed to import new data", e.getMessage());
            e.printStackTrace();
        }

        AppContext.getReportsController().refreshReportList();
    }

    // Theis Thomsen
    @FXML
    private void handleExport() {
        List<Group> groups;
        try {
            groups = GroupDAO.getAll();
        } catch (SQLException e) {
            Alerts.error("Export Failed", "Failed to load groups", e.getMessage());
            return;
        }

        if (groups.isEmpty()) {
            Alerts.error("Export Failed", "No groups found", "At least one group is required to export data.");
            return;
        }

        Group selectedGroup = pickGroup(groups);
        if (selectedGroup == null)
            return;

        List<Report> reports;
        try {
            reports = ReportDAO.findCompletedByGroupId(selectedGroup.id());
        } catch (SQLException e) {
            Alerts.error("Export Failed", "Failed to load reports for group", e.getMessage());
            return;
        }

        if (reports.isEmpty()) {
            Alerts.error("Export Failed", "No Reports", "No completed reports exist for the selected group.");
            return;
        }

        File file = chooseSaveCsvFile();
        if (file == null)
            return;

        try {
            int nRows = ExportService.exportToCSV(file, reports);
            Alerts.info("Export Successful",
                    "Successfully exported " + nRows + " visits to:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            Alerts.error("Export Failed", "IO Error", e.getMessage());
        }
    }

    private static Report pickReport(List<Report> reports) {
        ObservableList<String> options = FXCollections.observableArrayList();
        for (Report r : reports) {
            options.add(r.toString());
        }
        String selected = MenuSelectionWindowController.showAndWait(Main.getPrimaryStage(), "report", options);
        if (selected == null)
            return null;
        return reports.stream().filter(r -> r.toString().equals(selected)).findFirst().orElse(null);
    }

    private static Group pickGroup(List<Group> groups) {
        ObservableList<String> options = FXCollections.observableArrayList();
        for (Group group : groups) {
            options.add(group.name());
        }
        String selectedName = MenuSelectionWindowController.showAndWait(Main.getPrimaryStage(), "group", options);
        if (selectedName == null)
            return null;
        Group selected = groups.stream().filter(g -> g.name().equals(selectedName)).findFirst().orElse(null);
        if (selected == null) {
            Alerts.error("Export Failed", "Group not found", "Unable to locate the selected group.");
        }
        return selected;
    }

    // Theis Thomsen
    private static File chooseCsvFile(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(CSV_FILTER);
        return fileChooser.showOpenDialog(Main.getPrimaryStage());
    }

    private static File chooseSaveCsvFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export visits to CSV");
        chooser.getExtensionFilters().add(CSV_FILTER);
        return chooser.showSaveDialog(Main.getPrimaryStage());
    }

    // Theis Thomsen
    private static String promptForGroupName() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Specify Group Name");
        dialog.setHeaderText("Enter a name for the new group:");
        dialog.setContentText("Group name:");

        Optional<String> result = dialog.showAndWait();
        return result.map(String::trim).filter(s -> !s.isEmpty()).orElse(null);
    }
}
