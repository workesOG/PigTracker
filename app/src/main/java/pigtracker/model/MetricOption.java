// Theis Thomsen

package pigtracker.model;

import java.util.LinkedHashMap;
import java.util.Map;

import pigtracker.controller.components.FilterChipController.Operator;

public class MetricOption {
    public enum MetricType {
        STRING, INTEGER, DECIMAL, BOOLEAN, DATE
    }

    private final String label;
    private final Map<Operator, Boolean> enabledOperators = new LinkedHashMap<>();
    private final MetricType type;

    public MetricOption(String label, MetricType type, Boolean[] operatorBooleans) {
        if (operatorBooleans.length != 4) {
            throw new IllegalArgumentException("operatorBooleans must have a length of 4");
        }

        this.label = label;
        this.type = type;
        this.enabledOperators.put(Operator.EQUALS, operatorBooleans[0]);
        this.enabledOperators.put(Operator.GREATER_THAN, operatorBooleans[1]);
        this.enabledOperators.put(Operator.LESS_THAN, operatorBooleans[2]);
        this.enabledOperators.put(Operator.RANGE, operatorBooleans[3]);
    }

    public String getLabel() {
        return label;
    }

    public MetricType getType() {
        return type;
    }

    public Map<Operator, Boolean> getOperatorMap() {
        return enabledOperators;
    }

    @Override
    public String toString() {
        return label;
    }
}
