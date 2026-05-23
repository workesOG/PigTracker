package pigtracker.controller.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class FilterChipController {

    public enum Operator {
        LESS_THAN, GREATER_THAN, EQUALS, RANGE
    }

    @FXML
    private Label valueLabel;

    @FXML
    private Label ruleLabel;

    @FXML
    private Label removeButton;

    private String value;
    private Operator operator;

    private String num1;
    private String num2;

    private Runnable removeAction;

    public void setRule(String value, Operator operator, String num1) {

        setRule(value, operator, num1, null);
    }

    public void setRule(String value, Operator operator, String num1, String num2) {

        this.value = value;
        this.operator = operator;

        this.num1 = num1;
        this.num2 = num2;

        valueLabel.setText(value);

        switch (operator) {

        case LESS_THAN -> ruleLabel.setText("< " + num1);

        case GREATER_THAN -> ruleLabel.setText("> " + num1);

        case EQUALS -> ruleLabel.setText("= " + num1);

        case RANGE -> {

            if (num2 == null)
                throw new IllegalArgumentException("Range requires num2");

            ruleLabel.setText("[" + num1 + " - " + num2 + "]");
        }
        }
    }

    @FXML
    public void initialize() {
        removeButton.setOnMouseClicked(e -> {
            if (removeAction != null)
                removeAction.run();
        });
    }

    public void setRemoveAction(Runnable action) {

        this.removeAction = action;
    }

    public String getValue() {
        return value;
    }

    public Operator getOperator() {
        return operator;
    }

    public String getNum1() {
        return num1;
    }

    public String getNum2() {
        return num2;
    }
}