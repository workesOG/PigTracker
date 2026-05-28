// Theis Thomsen

package pigtracker.controller.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class TopThreePigsInfoPanelController {

    private static final int PIG_COUNT = 3;

    @FXML private Label metricLabel;

    @FXML private Label pig1Label;
    @FXML private Label pig2Label;
    @FXML private Label pig3Label;

    @FXML private Label pig1ValueLabel;
    @FXML private Label pig2ValueLabel;
    @FXML private Label pig3ValueLabel;

    public void setData(String metric, int[] pigNumbers, String[] pigValueStrings) {
        metricLabel.setText(metric);
        if (pigNumbers.length != PIG_COUNT || pigValueStrings.length != PIG_COUNT)
            return;

        Label[] numberLabels = { pig1Label, pig2Label, pig3Label };
        Label[] valueLabels = { pig1ValueLabel, pig2ValueLabel, pig3ValueLabel };

        for (int i = 0; i < PIG_COUNT; i++) {
            numberLabels[i].setText("#" + pigNumbers[i] + ":");
            valueLabels[i].setText(pigValueStrings[i]);
        }
    }
}
