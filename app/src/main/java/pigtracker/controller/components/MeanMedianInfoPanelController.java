// Theis Thomsen

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

    public void setData(String metric, String meanString, String medianString) {
        metricLabel.setText(metric);
        meanLabel.setText(meanString);
        medianLabel.setText(medianString);
    }
}
