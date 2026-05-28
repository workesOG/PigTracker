// Theis Thomsen

package pigtracker.model;

import java.util.LinkedHashMap;
import java.util.Map;

import pigtracker.controller.components.FilterChipController.Operator;

public class MetricOption {

    public enum MetricType {
        STRING, INTEGER, DECIMAL, BOOLEAN, DATE
    }

    private static final Operator[] OPERATOR_ORDER = {
            Operator.EQUALS, Operator.GREATER_THAN, Operator.LESS_THAN, Operator.RANGE
    };

    private final String label;
    private final MetricType type;
    private final Map<Operator, Boolean> enabledOperators = new LinkedHashMap<>();

    public MetricOption(String label, MetricType type, Boolean[] operatorBooleans) {
        if (operatorBooleans.length != OPERATOR_ORDER.length) {
            throw new IllegalArgumentException(
                    "operatorBooleans must have length " + OPERATOR_ORDER.length);
        }

        this.label = label;
        this.type = type;
        for (int i = 0; i < OPERATOR_ORDER.length; i++) {
            enabledOperators.put(OPERATOR_ORDER[i], operatorBooleans[i]);
        }
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
