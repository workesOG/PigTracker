// Theis Thomsen

package pigtracker.controller.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class InspectorFieldController {
    @FXML
    private Label valueLabel;

    @FXML
    private TextField valueField;

    public Label getValueLabel() {
        return valueLabel;
    }

    public TextField getValueField() {
        return valueField;
    }
}