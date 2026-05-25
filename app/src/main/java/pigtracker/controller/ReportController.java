// Theis Thomsen

package pigtracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.util.List;
import pigtracker.controller.components.MeanMedianInfoPanelController;
import pigtracker.controller.components.TopThreePigsInfoPanelController;
import pigtracker.model.MeanMedianMetric;
import pigtracker.model.TopThreePigs;

public class ReportController {
    @FXML
    private Label reportNumberLabel;

    @FXML
    private Label importDateLabel;

    @FXML
    private Label periodLabel;

    @FXML
    private Label dataRowsLabel;

    @FXML
    private Label numPigsLabel;

    @FXML
    private TopThreePigsInfoPanelController fcrPigInfoPanelController;

    @FXML
    private TopThreePigsInfoPanelController weightPigInfoPanelController;

    @FXML
    private TopThreePigsInfoPanelController feedPigInfoPanelController;

    @FXML
    private TopThreePigsInfoPanelController mostVisitsPigInfoPanelController;

    @FXML
    private TopThreePigsInfoPanelController longestVisitsPigInfoPanelController;

    @FXML
    private MeanMedianInfoPanelController fcrMeanAndMedianValuesPanelController;

    @FXML
    private MeanMedianInfoPanelController weightMeanAndMedianValuesPanelController;

    @FXML
    private MeanMedianInfoPanelController dailyFeedMeanAndMedianValuesPanelController;

    @FXML
    private MeanMedianInfoPanelController visitFeedMeanAndMedianValuesPanelController;

    @FXML
    private MeanMedianInfoPanelController visitsPerPigMeanAndMedianValuesPanelController;

    @FXML
    private MeanMedianInfoPanelController visitLengthMeanAndMedianValuesPanelController;

    public void setMetadata(String reportNumber, String importDate, String period, String dataRows, String numPigs) {
        reportNumberLabel.setText(reportNumber);
        importDateLabel.setText(importDate);
        periodLabel.setText(period);
        dataRowsLabel.setText(dataRows);
        numPigsLabel.setText(numPigs);
    }

    public void setTopPigPanels(List<TopThreePigs> panels) {
        if (panels.size() != 5) {
            throw new IllegalArgumentException("Expected exactly 5 panels of data");
        }

        TopThreePigsInfoPanelController[] controllers = { fcrPigInfoPanelController, weightPigInfoPanelController,
                feedPigInfoPanelController, mostVisitsPigInfoPanelController, longestVisitsPigInfoPanelController };

        for (int i = 0; i < controllers.length && i < panels.size(); i++) {
            var data = panels.get(i);
            if (data != null && controllers[i] != null) {
                controllers[i].setData(data.metric(), data.pigNumbers(), data.pigValues());
            }
        }
    }

    public void setMeanMedianPanels(List<MeanMedianMetric> panels) {
        if (panels.size() != 6) {
            throw new IllegalArgumentException("Expected exactly 6 panels of data");
        }

        MeanMedianInfoPanelController[] controllers = { fcrMeanAndMedianValuesPanelController,
                weightMeanAndMedianValuesPanelController, dailyFeedMeanAndMedianValuesPanelController,
                visitFeedMeanAndMedianValuesPanelController, visitsPerPigMeanAndMedianValuesPanelController,
                visitLengthMeanAndMedianValuesPanelController };

        for (int i = 0; i < controllers.length && i < panels.size(); i++) {
            var data = panels.get(i);
            if (data != null && controllers[i] != null) {
                controllers[i].setData(data.metric(), data.mean(), data.median());
            }
        }
    }
}
