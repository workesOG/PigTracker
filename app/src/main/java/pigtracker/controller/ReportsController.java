package pigtracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import pigtracker.model.Report;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

public class ReportsController {
    @FXML
    private ListView<String> reportList;

    @FXML
    private AnchorPane reportContainer;

    public void loadReportDetailView(Report report) {
        if (report == null)
            return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/report-view.fxml"));
            AnchorPane reportPane = loader.load();
            ReportController controller = loader.getController();

            controller.setMetadata(report.reportNumber(), report.importDate(), report.period(), report.dataRows(),
                    report.numPigs());
            controller.setMeanMedianPanels(report.meanMedianPanels());
            controller.setTopPigPanels(report.topPigPanels());

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

    @FXML
    public void initialize() {

    }
}
