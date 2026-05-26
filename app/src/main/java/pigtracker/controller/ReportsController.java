// Theis Thomsen

package pigtracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import pigtracker.model.Report;
import pigtracker.model.ReportMetrics;
import pigtracker.util.AppContext;
import pigtracker.dao.ReportDAO;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ReportsController {
    @FXML
    private ListView<Report> reportList;

    @FXML
    private AnchorPane reportContainer;

    public void loadReportDetailView(Report report) {
        if (report == null)
            return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/report-view.fxml"));
            AnchorPane reportPane = loader.load();
            ReportController controller = loader.getController();

            controller.setMetadata(report.id(), report.createdAt(), report.importStart(), report.importEnd(),
                    report.rowCount(), report.pigCount(), report.createdBy());

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

    public void refreshReportList() {
        try {
            List<Report> reports = ReportDAO.getAllCompleted();
            reportList.getItems().setAll(reports);
        } catch (SQLException e) {
            e.printStackTrace();
            reportList.getItems().setAll(Report.getReportListErrorReport());
        }
    }
    
    @FXML
    public void initialize() {
        AppContext.setReportsController(this);
        refreshReportList();
        reportList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadReportDetailView(newSelection);
            }
        });
    }
}
