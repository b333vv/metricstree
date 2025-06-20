package org.b333vv.metric.model.metric.value;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DerivativeMetricsRangeTest {

    private final Value v5 = Value.of(5);
    private final Value v10 = Value.of(10);
    private final Value v15 = Value.of(15);
    private final Value vUndefined = Value.UNDEFINED;

    // 1. `of` method
    @Test
    void testOfMethodValidInputs() {
        DerivativeMetricsRange range = DerivativeMetricsRange.of(v5, v15); // Regular: [5-15)
        assertNotNull(range);
        assertNotEquals(DerivativeMetricsRange.UNDEFINED, range);
        assertEquals(v5, range.getRegularFrom());
        assertEquals(v15, range.getRegularTo());
    }

    @Test
    void testOfMethodUndefinedInputs() {
        assertEquals(DerivativeMetricsRange.UNDEFINED, DerivativeMetricsRange.of(vUndefined, v15));
        assertEquals(DerivativeMetricsRange.UNDEFINED, DerivativeMetricsRange.of(v5, vUndefined));
        assertEquals(DerivativeMetricsRange.UNDEFINED, DerivativeMetricsRange.of(vUndefined, vUndefined));
    }

    @Test
    void testOfMethodInvalidRangeOrder() {
        // from > to
        assertEquals(DerivativeMetricsRange.UNDEFINED, DerivativeMetricsRange.of(v15, v5));
    }

    // 2. `getRangeType` method
    @Test
    void testGetRangeType() {
        DerivativeMetricsRange range = DerivativeMetricsRange.of(v5, v15); // Regular: [5-15)

        assertEquals(RangeType.UNDEFINED, range.getRangeType(vUndefined), "Undefined value should be RangeType.UNDEFINED");

        // REGULAR
        assertEquals(RangeType.REGULAR, range.getRangeType(v5), "Value 5 (boundary) should be REGULAR");
        assertEquals(RangeType.REGULAR, range.getRangeType(v10), "Value 10 should be REGULAR");
        assertEquals(RangeType.REGULAR, range.getRangeType(Value.of(14.99)), "Value 14.99 should be REGULAR");

        // HIGH (values outside the from-to range)
        assertEquals(RangeType.HIGH, range.getRangeType(Value.of(4.99)), "Value 4.99 (below from) should be HIGH");
        assertEquals(RangeType.HIGH, range.getRangeType(Value.of(0)), "Value 0 (below from) should be HIGH");
        assertEquals(RangeType.HIGH, range.getRangeType(v15), "Value 15 (boundary - to) should be HIGH");
        assertEquals(RangeType.HIGH, range.getRangeType(Value.of(20)), "Value 20 (above to) should be HIGH");

        // Test with DerivativeMetricsRange.UNDEFINED
        assertEquals(RangeType.UNDEFINED, DerivativeMetricsRange.UNDEFINED.getRangeType(v10));
    }

    // 3. Getter methods
    @Test
    void testGetterMethods() {
        DerivativeMetricsRange range = DerivativeMetricsRange.of(v5, v15);
        assertEquals(v5, range.getRegularFrom(), "getRegularFrom");
        assertEquals(v15, range.getRegularTo(), "getRegularTo");

        // For DerivativeMetricsRange, High, VeryHigh, Extreme ranges are not explicitly defined with different 'to' values.
        // They conceptually represent everything outside 'regular'.
        // The specific from/to for these might be UNDEFINED or represent open-ended intervals.
        // Based on typical derivative range behavior, only regular from/to are primary.
        // Let's assume other getters return UNDEFINED as they are not applicable for distinct ranges.
        assertEquals(Value.UNDEFINED, range.getHighFrom(), "getHighFrom should be UNDEFINED or represent open start");
        assertEquals(Value.UNDEFINED, range.getHighTo(), "getHighTo should be UNDEFINED");
        assertEquals(Value.UNDEFINED, range.getVeryHighFrom(), "getVeryHighFrom should be UNDEFINED");
        assertEquals(Value.UNDEFINED, range.getVeryHighTo(), "getVeryHighTo should be UNDEFINED");
        assertEquals(Value.UNDEFINED, range.getExtremeFrom(), "getExtremeFrom should be UNDEFINED");
        assertEquals(Value.UNDEFINED, range.getExtremeTo(), "getExtremeTo should be UNDEFINED");


        // Test getters for DerivativeMetricsRange.UNDEFINED
        assertEquals(vUndefined, DerivativeMetricsRange.UNDEFINED.getRegularFrom());
        assertEquals(vUndefined, DerivativeMetricsRange.UNDEFINED.getRegularTo());
        assertEquals(vUndefined, DerivativeMetricsRange.UNDEFINED.getHighFrom());
        // ... and so on for all getters of UNDEFINED range
    }

    // 4. `toString` and `percentageFormat` methods
    @Test
    void testToStringAndPercentageFormat() {
        DerivativeMetricsRange range = DerivativeMetricsRange.of(Value.of(0.25), Value.of(0.75));
        // Example: Regular: [0.25-0.75)
        String expectedString = "[0.25-0.75)"; // Only the regular range is typically shown
        assertEquals(expectedString, range.toString());

        String expectedPercentageString = "[25.00%-75.00%)";
        assertEquals(expectedPercentageString, range.percentageFormat());

        assertEquals("", DerivativeMetricsRange.UNDEFINED.toString(), "toString for UNDEFINED range should be empty");
        assertEquals("", DerivativeMetricsRange.UNDEFINED.percentageFormat(), "percentageFormat for UNDEFINED range should be empty");
    }

    // 5. `getRangeByRangeType` method
    @Test
    void testGetRangeByRangeType() {
        DerivativeMetricsRange range = DerivativeMetricsRange.of(v5, v15); // Regular: [5-15)

        assertEquals("[5-15)", range.getRangeByRangeType(RangeType.REGULAR));

        // For DerivativeMetricsRange, other types might return empty or a representation of "outside regular"
        // Based on the problem, expecting empty for others seems reasonable.
        assertEquals("", range.getRangeByRangeType(RangeType.HIGH));
        assertEquals("", range.getRangeByRangeType(RangeType.VERY_HIGH));
        assertEquals("", range.getRangeByRangeType(RangeType.EXTREME));
        assertEquals("", range.getRangeByRangeType(RangeType.UNDEFINED));

        // Test with DerivativeMetricsRange.UNDEFINED
        assertEquals("", DerivativeMetricsRange.UNDEFINED.getRangeByRangeType(RangeType.REGULAR));
        assertEquals("", DerivativeMetricsRange.UNDEFINED.getRangeByRangeType(RangeType.UNDEFINED));

        DerivativeMetricsRange percentRange = DerivativeMetricsRange.of(Value.of(0.25), Value.of(0.75));
        assertEquals("[0.25-0.75)", percentRange.getRangeByRangeType(RangeType.REGULAR)); // Uses Value.toString
    }
}
