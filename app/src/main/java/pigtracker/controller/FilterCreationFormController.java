// Theis Thomsen

package pigtracker.controller;

import java.util.List;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import pigtracker.controller.components.FilterChipContainerController;
import pigtracker.controller.components.FilterChipController.Operator;
import pigtracker.controller.components.SegmentedToggleController;
import pigtracker.model.MetricOption;
import pigtracker.model.MetricOption.MetricType;

public class FilterCreationFormController {

    @FXML private Label valueSectionLabel;
    @FXML private Label value1Label;
    @FXML private Label value2Label;
    @FXML private Button finalizeButton;
    @FXML private Button discardButton;
    @FXML private ChoiceBox<MetricOption> metricDropdown;
    @FXML private TextField value1TextField;
    @FXML private TextField value2TextField;
    @FXML private SegmentedToggleController conditionSegmentedToggleController;

    private FilterChipContainerController filterChipContainerController;

    @FXML
    public void initialize() {
        conditionSegmentedToggleController.setOptions("=", ">", "<", "Range");

        metricDropdown.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> onSelectedOptionChange(newValue));

        conditionSegmentedToggleController.addSelectionChangeListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                updateValueSection(Operator.fromLabel(newSelection));
            }
        });

        value1TextField.textProperty().addListener((obs, oldVal, newVal) -> updateFinalizeButton());
        value2TextField.textProperty().addListener((obs, oldVal, newVal) -> updateFinalizeButton());
    }

    public void setupForm(List<MetricOption> metricOptions) {
        metricDropdown.getItems().clear();
        for (MetricOption option : metricOptions) {
            if (option.getType() != MetricType.DATE) {
                metricDropdown.getItems().add(option);
            }
        }
        metricDropdown.getSelectionModel().selectFirst();
        updateValueSection(Operator.fromLabel(conditionSegmentedToggleController.getSelected()));
    }

    public void setFilterChipContainerController(FilterChipContainerController controller) {
        this.filterChipContainerController = controller;
    }

    @FXML
    private void cancelFilterCreation() {
        closePopup();
    }

    @FXML
    private void finalizeFilterCreation() {
        MetricOption selectedMetric = metricDropdown.getSelectionModel().getSelectedItem();
        Operator selectedOperator = Operator.fromLabel(conditionSegmentedToggleController.getSelected());
        String value1 = value1TextField.getText();
        String value2 = value2TextField.getText();

        if (!isFilterValid(selectedMetric, selectedOperator, value1, value2))
            return;

        if (selectedOperator == Operator.RANGE) {
            filterChipContainerController.addFilter(selectedMetric, selectedOperator, value1, value2);
        } else {
            filterChipContainerController.addFilter(selectedMetric, selectedOperator, value1);
        }
        closePopup();
    }

    private void onSelectedOptionChange(MetricOption newSelection) {
        updateSegmentedToggle(newSelection.getOperatorMap());
        updateFinalizeButton();
    }

    private void updateSegmentedToggle(Map<Operator, Boolean> allowedOperators) {
        Operator currentlySelected = Operator.fromLabel(conditionSegmentedToggleController.getSelected());
        boolean oldSelectionDisabled = !allowedOperators.get(currentlySelected);
        boolean hasEnabledFirstButton = false;

        for (Map.Entry<Operator, Boolean> entry : allowedOperators.entrySet()) {
            Operator operator = entry.getKey();
            boolean enabled = entry.getValue();

            conditionSegmentedToggleController.enableToggle(operator.label(), enabled);

            if (oldSelectionDisabled && enabled && !hasEnabledFirstButton) {
                conditionSegmentedToggleController.setSelected(operator.label());
                hasEnabledFirstButton = true;
            }
        }
    }

    private void updateValueSection(Operator operator) {
        switch (operator) {
            case EQUALS -> applyValueSectionChanges("Equal to:", null);
            case GREATER_THAN -> applyValueSectionChanges("Greater than:", null);
            case LESS_THAN -> applyValueSectionChanges("Less than:", null);
            case RANGE -> applyValueSectionChanges("Between:", "and:       ");
        }
        updateFinalizeButton();
    }

    private void updateFinalizeButton() {
        MetricOption selectedMetric = metricDropdown.getSelectionModel().getSelectedItem();
        Operator selectedOperator = Operator.fromLabel(conditionSegmentedToggleController.getSelected());
        String value1 = value1TextField.getText();
        String value2 = value2TextField.getText();

        finalizeButton.setDisable(!isFilterValid(selectedMetric, selectedOperator, value1, value2));
    }

    private void applyValueSectionChanges(String label1, String label2) {
        boolean showLabel2 = label2 != null;
        value1Label.setText(label1);
        value2Label.setVisible(showLabel2);
        value2TextField.setVisible(showLabel2);
        if (showLabel2) {
            value2Label.setText(label2);
        }
    }

    private static boolean isFilterValid(MetricOption selectedMetric, Operator selectedOperator,
            String value1, String value2) {
        MetricType expectedType = selectedMetric.getType();
        Object value1Obj = parseAs(expectedType, value1);
        if (value1Obj == null)
            return false;

        if (selectedOperator != Operator.RANGE)
            return true;

        Object value2Obj = parseAs(expectedType, value2);
        return value2Obj != null && isOrdered(value1Obj, value2Obj);
    }

    private static Object parseAs(MetricType type, String value) {
        return switch (type) {
            case STRING -> value;
            case INTEGER -> tryParseInt(value);
            case DECIMAL -> tryParseDouble(value);
            case BOOLEAN -> parseBoolean(value);
            default -> null;
        };
    }

    private static Integer tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double tryParseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Boolean parseBoolean(String value) {
        if (value.equalsIgnoreCase("true")) return true;
        if (value.equalsIgnoreCase("false")) return false;
        return null;
    }

    private static boolean isOrdered(Object value1, Object value2) {
        if (!(value1 instanceof Number) || !(value2 instanceof Number)) {
            throw new IllegalArgumentException("Values must be numeric");
        }
        return ((Number) value1).doubleValue() <= ((Number) value2).doubleValue();
    }

    private void closePopup() {
        ((Stage) finalizeButton.getScene().getWindow()).close();
    }
}
