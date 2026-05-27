// Theis Thomsen

package pigtracker.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.time.LocalDateTime;
import java.util.List;
import pigtracker.controller.components.MeanMedianInfoPanelController;
import pigtracker.controller.components.TopThreePigsInfoPanelController;
import pigtracker.dao.GroupDAO;
import pigtracker.model.Group;
import pigtracker.model.MeanMedianMetric;
import pigtracker.model.TopThreePigs;
import pigtracker.util.DateFormattingUtil;

public class ReportController {
    @FXML
    private Label reportNumberLabel;

    @FXML
    private Label groupNameLabel;

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

    @FXML
    private AreaChart<String, Number> activityDensityGraph;

    public void setMetadata(int reportNumber, int groupId, LocalDateTime importDate, LocalDateTime periodStart,
            LocalDateTime periodEnd, int dataRows, int numPigs, int creatorUserID) {
        reportNumberLabel.setText("Report #" + reportNumber);

        try {
            Group group = GroupDAO.findById(groupId).orElse(null);
            if (group != null) {
                groupNameLabel.setText(group.name());
            } else {
                throw new IllegalArgumentException("Group with ID " + groupId + " not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        importDateLabel.setText(importDate.format(DateFormattingUtil.dateTimeFormatterNoSeconds));
        periodLabel.setText(periodStart.format(DateFormattingUtil.dateFormatter) + " - "
                + periodEnd.format(DateFormattingUtil.dateFormatter));

        dataRowsLabel.setText(String.valueOf(dataRows));
        numPigsLabel.setText(String.valueOf(numPigs));
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
                controllers[i].setData(data.metric(), data.pigNumbers(), data.getDisplayStrings());
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
                controllers[i].setData(data.metric(), data.getMeanDisplayString(), data.getMedianDisplayString());
            }
        }
    }

    public void setActivityDensityGraph(List<Integer> activityByHour) {
        activityDensityGraph.setLegendVisible(false);

        CategoryAxis xAxis = (CategoryAxis)activityDensityGraph.getXAxis();
        xAxis.setGapStartAndEnd(false);
        xAxis.setStartMargin(0);
        xAxis.setEndMargin(0);

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (int hour = 0; hour < activityByHour.size(); hour++) {
            String hourLabel = String.format("%02d-%02d", hour, (hour + 1) % 24);
            series.getData().add(new XYChart.Data<>(hourLabel, activityByHour.get(hour)));
        }

        activityDensityGraph.getData().clear();
        activityDensityGraph.getData().add(series);
        activityDensityGraph.getXAxis().setLabel("Hour of Day");
        activityDensityGraph.getYAxis().setLabel("Number of Visits");
    }
}
