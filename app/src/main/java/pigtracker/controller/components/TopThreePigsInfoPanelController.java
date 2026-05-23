package pigtracker.controller.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class TopThreePigsInfoPanelController {
    @FXML
    private Label metricLabel;

    @FXML
    private Label pig1Label;

    @FXML
    private Label pig2Label;

    @FXML
    private Label pig3Label;

    @FXML
    private Label pig1ValueLabel;

    @FXML
    private Label pig2ValueLabel;

    @FXML
    private Label pig3ValueLabel;

    public void setData(String metric, int[] pigNumbers, double[] pigValues) {
        metricLabel.setText(metric);

        if (pigNumbers.length == 3 && pigValues.length == 3) {
            pig1Label.setText("Pig #" + pigNumbers[0] + ":");
            pig2Label.setText("Pig #" + pigNumbers[1] + ":");
            pig3Label.setText("Pig #" + pigNumbers[2] + ":");

            pig1ValueLabel.setText(String.format("%.2f", pigValues[0]));
            pig2ValueLabel.setText(String.format("%.2f", pigValues[1]));
            pig3ValueLabel.setText(String.format("%.2f", pigValues[2]));
        }
    }
}
