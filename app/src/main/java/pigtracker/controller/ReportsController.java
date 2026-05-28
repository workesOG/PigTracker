// Theis Thomsen

package pigtracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import pigtracker.model.Report;
import pigtracker.model.ReportMetrics;
import pigtracker.util.AppContext;
import pigtracker.dao.ReportDAO;
import pigtracker.dao.VisitDAO;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ReportsController {
    @FXML
    private ListView<Report> reportList;

    @FXML
    private AnchorPane reportContainer;

    @FXML
    private Button deleteReportButton;

    @FXML
    public void initialize() {
        AppContext.setReportsController(this);
        refreshReportList();

        deleteReportButton.disableProperty().bind(reportList.getSelectionModel().selectedItemProperty().isNull());

        reportList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadReportDetailView(newSelection);
            }
        });
    }

    @FXML
    private void onDeleteReport() {
        Report selectedReport = reportList.getSelectionModel().getSelectedItem();
        if (selectedReport == null)
            return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete this report (and all its visits)?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Delete Report");
        confirm.setHeaderText(null);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.YES)
            return;

        try {
            VisitDAO.deleteByReportId(selectedReport.id());
            ReportDAO.delete(selectedReport.id());
            refreshReportList();
            reportContainer.getChildren().clear();
        } catch (SQLException ex) {
            ex.printStackTrace();
            Alert error = new Alert(Alert.AlertType.ERROR, "Failed to delete report: " + ex.getMessage());
            error.setHeaderText("Delete failed");
            error.showAndWait();
        }
    }

    public void loadReportDetailView(Report report) {
        if (report == null)
            return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/report-view.fxml"));
            AnchorPane reportPane = loader.load();
            ReportController controller = loader.getController();

            controller.setMetadata(report.id(), report.groupId(), report.createdAt(), report.importStart(),
                    report.importEnd(), report.rowCount(), report.pigCount(), report.createdBy());

            try {
                ReportMetrics metrics = pigtracker.service.ReportImportService.generateReportData(report);
                controller.setMeanMedianPanels(metrics.meanMedian());
                controller.setTopPigPanels(metrics.topThree());
                controller.setActivityDensityGraph(metrics.activityByHour());
            } catch (Exception e) {
                e.printStackTrace();
            }

            reportContainer.getChildren().clear();
            reportContainer.getChildren().add(reportPane);
            AnchorPane.setTopAnchor(reportPane, 0.0);
            AnchorPane.setBottomAnchor(reportPane, 0.0);
            AnchorPane.setLeftAnchor(reportPane, 0.0);
            AnchorPane.setRightAnchor(reportPane, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clearReportView() {
        reportContainer.getChildren().clear();
        reportList.getSelectionModel().clearSelection();
    }

    public void refreshReportList() {
        try {
            List<Report> reports = ReportDAO.getAllCompleted();
            reportList.getItems().setAll(reports);
        } catch (SQLException e) {
            e.printStackTrace();
            reportList.getItems().setAll(Report.getReportListErrorReport());
        }
    }
}
