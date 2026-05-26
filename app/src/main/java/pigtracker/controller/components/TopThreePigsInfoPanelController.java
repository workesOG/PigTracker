// Theis Thomsen

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

    public void setData(String metric, int[] pigNumbers, String[] pigValueStrings) {
        metricLabel.setText(metric);

        if (pigNumbers.length == 3 && pigValueStrings.length == 3) {
            pig1Label.setText("#" + pigNumbers[0] + ":");
            pig2Label.setText("#" + pigNumbers[1] + ":");
            pig3Label.setText("#" + pigNumbers[2] + ":");

            pig1ValueLabel.setText(pigValueStrings[0]);
            pig2ValueLabel.setText(pigValueStrings[1]);
            pig3ValueLabel.setText(pigValueStrings[2]);
        }
    }
}
