package pigtracker.controller;

import java.util.ArrayList;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import pigtracker.controller.components.SegmentedToggleController;
import pigtracker.controller.components.FilterChipContainerController;
import pigtracker.controller.components.FilterChipController.Operator;
import pigtracker.model.MetricOption;
import pigtracker.model.MetricOption.MetricType;

public class FilterCreationFormController {
    @FXML
    private Label valueSectionLabel;

    @FXML
    private Label value1Label;

    @FXML
    private Label value2Label;

    @FXML
    private Button finalizeButton;

    @FXML
    private Button discardButton;

    @FXML
    private ChoiceBox<MetricOption> metricDropdown;

    @FXML
    private TextField value1TextField;

    @FXML
    private TextField value2TextField;

    @FXML
    private SegmentedToggleController conditionSegmentedToggleController;

    private FilterChipContainerController filterChipContainerController;

    @FXML
    public void initialize() {
        setupSegmentedToggle();

        metricDropdown.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            onSelectedOptionChange(newValue);
        });

        conditionSegmentedToggleController.addSelectionChangeListener((observable, oldSelection, newSelection) -> {
            if (newSelection != null) {
                updateValueSection(labelToOperator(newSelection));
            }
        });

        value1TextField.textProperty().addListener((obs, oldVal, newVal) -> updateFinalizeButton());

        value2TextField.textProperty().addListener((obs, oldVal, newVal) -> updateFinalizeButton());
    }

    public void setupForm(ArrayList<MetricOption> metricOptions) {
        metricDropdown.getItems().clear();
        for (MetricOption option : metricOptions) {
            if (option.getType() != MetricType.DATE) {
                metricDropdown.getItems().add(option);
            }
        }
        metricDropdown.getSelectionModel().selectFirst();
        updateValueSection(labelToOperator(conditionSegmentedToggleController.getSelected()));
    }

    public void setFilterChipContainerController(FilterChipContainerController filterChipContainerController) {
        this.filterChipContainerController = filterChipContainerController;
    }

    @FXML
    private void cancelFilterCreation() {
        closePopup();
    }

    @FXML
    private void finalizeFilterCreation() {
        MetricOption selectedMetric = metricDropdown.getSelectionModel().getSelectedItem();
        Operator selectedOperator = labelToOperator(conditionSegmentedToggleController.getSelected());
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

    private void setupSegmentedToggle() {
        conditionSegmentedToggleController.setOptions("=", ">", "<", "Range");
    }

    private void onSelectedOptionChange(MetricOption newSelection) {
        updateSegmentedToggle(newSelection.getOperatorMap());
        updateFinalizeButton();
    }

    private void updateSegmentedToggle(Map<Operator, Boolean> allowedOperators) {
        Boolean hasEnabledFirstButton = false;
        Boolean oldSelectionDisabled = !allowedOperators
                .get(labelToOperator(conditionSegmentedToggleController.getSelected()));
        System.out.println(oldSelectionDisabled);

        for (Map.Entry<Operator, Boolean> entry : allowedOperators.entrySet()) {
            Boolean enabled = entry.getValue();
            Operator operator = entry.getKey();

            conditionSegmentedToggleController.enableToggle(operatorToLabel(operator), enabled);

            if (oldSelectionDisabled && enabled && !hasEnabledFirstButton) {
                conditionSegmentedToggleController.setSelected(operatorToLabel(operator));
                hasEnabledFirstButton = true;
            }
        }
    }

    private void updateValueSection(Operator operator) {
        switch (operator) {
        case Operator.EQUALS:
            applyValueSectionChanges("Equal to:", null);
            break;
        case Operator.GREATER_THAN:
            applyValueSectionChanges("Greater than:", null);
            break;
        case Operator.LESS_THAN:
            applyValueSectionChanges("Less than:", null);
            break;
        case Operator.RANGE:
            applyValueSectionChanges("Between:", "and:       ");
            break;
        default:
            throw new IllegalArgumentException("Invalid operator");
        }

        updateFinalizeButton();
    }

    private void updateFinalizeButton() {
        MetricOption selectedMetric = metricDropdown.getSelectionModel().getSelectedItem();
        Operator selectedOperator = labelToOperator(conditionSegmentedToggleController.getSelected());
        String value1 = value1TextField.getText();
        String value2 = value2TextField.getText();

        finalizeButton.setDisable(!isFilterValid(selectedMetric, selectedOperator, value1, value2));
    }

    private void applyValueSectionChanges(String label1, String label2) {
        Boolean showLabel2 = label2 != null;

        value1Label.setText(label1);
        value2Label.setVisible(showLabel2);
        value2TextField.setVisible(showLabel2);

        if (showLabel2) {
            value2Label.setText(label2);
        }
    }

    private boolean isFilterValid(MetricOption selectedMetric, Operator selectedOperator, String value1,
            String value2) {
        MetricType expectedType = selectedMetric.getType();

        Object value1Obj = getValueAsExpectedType(expectedType, value1);
        if (value1Obj == null)
            return false;

        if (selectedOperator == Operator.RANGE) {
            Object value2Obj = getValueAsExpectedType(expectedType, value2);
            if (value2Obj == null)
                return false;
            return isOrdered(value1Obj, value2Obj);
        }

        return true;
    }

    private Object getValueAsExpectedType(MetricType expectedType, String value) {

        switch (expectedType) {

        case STRING:
            return value;

        case INTEGER:
            try {
                return Integer.parseInt(value);

            } catch (NumberFormatException e) {
                return null;
            }

        case DECIMAL:
            try {
                return Double.parseDouble(value);

            } catch (NumberFormatException e) {
                return null;
            }

        case BOOLEAN:

            if (value.equalsIgnoreCase("true"))
                return true;

            if (value.equalsIgnoreCase("false"))
                return false;

            return null;

        default:
            return null;
        }
    }

    private boolean isOrdered(Object value1, Object value2) {

        if (!(value1 instanceof Number) || !(value2 instanceof Number)) {

            throw new IllegalArgumentException("Values must be numeric");
        }

        return ((Number)value1).doubleValue() <= ((Number)value2).doubleValue();
    }

    private String operatorToLabel(Operator operator) {
        switch (operator) {
        case Operator.EQUALS:
            return "=";
        case Operator.GREATER_THAN:
            return ">";
        case Operator.LESS_THAN:
            return "<";
        case Operator.RANGE:
            return "Range";
        default:
            throw new IllegalArgumentException("Invalid operator");
        }
    }

    private Operator labelToOperator(String label) {
        switch (label) {
        case "=":
            return Operator.EQUALS;
        case ">":
            return Operator.GREATER_THAN;
        case "<":
            return Operator.LESS_THAN;
        case "Range":
            return Operator.RANGE;
        default:
            throw new IllegalArgumentException("Invalid label");
        }
    }

    private void closePopup() {
        Stage stage = (Stage)finalizeButton.getScene().getWindow();
        stage.close();
    }
}
