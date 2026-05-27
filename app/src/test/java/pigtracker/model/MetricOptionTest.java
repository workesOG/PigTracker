// Af Nikolaj Jakobsen

package pigtracker.model;

import org.junit.jupiter.api.Test;
import pigtracker.controller.components.FilterChipController.Operator;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetricOptionTest {

    @Test
    void rejectsOperatorBooleansThatAreNotLengthFour() {
        // The constructor expects exactly one flag per operator.
        assertThrows(IllegalArgumentException.class,
                () -> new MetricOption("Weight", MetricOption.MetricType.DECIMAL, new Boolean[] { true, false }));
    }

    @Test
    void mapsBooleansToOperatorsInFixedOrder() {
        // The booleans are assigned in the order EQUALS, GREATER_THAN, LESS_THAN, RANGE.
        MetricOption option = new MetricOption("Weight", MetricOption.MetricType.DECIMAL,
                new Boolean[] { true, false, true, false });

        Map<Operator, Boolean> operators = option.getOperatorMap();
        assertTrue(operators.get(Operator.EQUALS));
        assertFalse(operators.get(Operator.GREATER_THAN));
        assertTrue(operators.get(Operator.LESS_THAN));
        assertFalse(operators.get(Operator.RANGE));
    }

    @Test
    void exposesLabelAndType() {
        MetricOption option = new MetricOption("Weight", MetricOption.MetricType.DECIMAL,
                new Boolean[] { true, true, true, true });

        assertEquals("Weight", option.getLabel());
        assertEquals(MetricOption.MetricType.DECIMAL, option.getType());
    }

    @Test
    void toStringReturnsLabel() {
        MetricOption option = new MetricOption("FCR", MetricOption.MetricType.DECIMAL,
                new Boolean[] { true, true, true, true });

        assertEquals("FCR", option.toString());
    }
}
