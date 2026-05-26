package pigtracker.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MathUtilTest {
    @Test
    void meanReturnsZeroForEmptyList() {
        assertEquals(0.0, MathUtil.mean(List.of()), 1e-9);
    }

    @Test
    void meanAveragesValues() {
        assertEquals(3.5, MathUtil.mean(List.of(1.0, 3.0, 6.5)), 1e-9);
    }

    @Test
    void medianReturnsZeroForEmptyList() {
        assertEquals(0.0, MathUtil.median(List.of()), 1e-9);
    }

    @Test
    void medianReturnsMiddleValueForOddUnsortedList() {
        assertEquals(4.0, MathUtil.median(List.of(8.0, 1.0, 4.0)), 1e-9);
    }

    @Test
    void medianAveragesMiddleValuesForEvenUnsortedList() {
        assertEquals(4.5, MathUtil.median(List.of(8.0, 1.0, 5.0, 4.0)), 1e-9);
    }

    @Test
    void medianDoesNotMutateInputList() {
        List<Double> values = new ArrayList<>(List.of(8.0, 1.0, 5.0, 4.0));
        List<Double> original = new ArrayList<>(values);

        MathUtil.median(values);

        assertEquals(original, values);
    }
}
