package pigtracker.controller.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MeanMedianInfoPanelController {
    @FXML
    private Label metricLabel;

    @FXML
    private Label meanLabel;

    @FXML
    private Label medianLabel;

    public void setData(String metric, double mean, double median) {
        metricLabel.setText(metric);
        meanLabel.setText(String.format("Mean: %.2f", mean));
        medianLabel.setText(String.format("Median: %.2f", median));
    }
}
