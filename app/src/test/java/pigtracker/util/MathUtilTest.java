// Af Nikolaj Jakobsen

package pigtracker.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MathUtilTest {

    private static final double DELTA = 1e-9;

    @Test
    void meanReturnsZeroForEmptyList() {
        assertEquals(0.0, MathUtil.mean(List.of()), DELTA);
    }

    @Test
    void meanAveragesValues() {
        assertEquals(5.0, MathUtil.mean(List.of(2.0, 4.0, 9.0)), DELTA);
    }

    @Test
    void medianReturnsZeroForEmptyList() {
        assertEquals(0.0, MathUtil.median(List.of()), DELTA);
    }

    @Test
    void medianOfOddCountReturnsMiddleValue() {
        // Input is unsorted; median should sort first and return the middle element.
        assertEquals(3.0, MathUtil.median(List.of(5.0, 1.0, 3.0)), DELTA);
    }

    @Test
    void medianOfEvenCountAveragesTheTwoMiddleValues() {
        // Sorted: [1, 2, 3, 4] -> average of 2 and 3.
        assertEquals(2.5, MathUtil.median(List.of(4.0, 1.0, 3.0, 2.0)), DELTA);
    }

    @Test
    void percentChangeReturnsZeroWhenOldValueIsZero() {
        // Avoids division by zero by treating a zero baseline as no change.
        assertEquals(0.0, MathUtil.percentChange(0.0, 50.0), DELTA);
    }

    @Test
    void percentChangeComputesIncrease() {
        assertEquals(50.0, MathUtil.percentChange(50.0, 75.0), DELTA);
    }

    @Test
    void percentChangeUsesAbsoluteValueOfOldValue() {
        // The denominator is |oldValue|, so a -50 -> -25 move is a +50% change, not -50%.
        assertEquals(50.0, MathUtil.percentChange(-50.0, -25.0), DELTA);
    }
}
