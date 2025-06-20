package org.b333vv.metric.model.metric.value;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BasicMetricsRangeTest {

    private final Value v0 = Value.of(0);
    private final Value v10 = Value.of(10);
    private final Value v20 = Value.of(20);
    private final Value v30 = Value.of(30);
    private final Value vMinus5 = Value.of(-5);
    private final Value v15 = Value.of(15);
    private final Value v25 = Value.of(25);
    private final Value v35 = Value.of(35);
    private final Value vUndefined = Value.UNDEFINED;

    // 1. `of` method
    @Test
    void testOfMethodValidInputs() {
        BasicMetricsRange range = BasicMetricsRange.of(v10, v20, v30);
        assertNotNull(range);
        assertNotEquals(BasicMetricsRange.UNDEFINED, range);
        assertEquals(v0, range.getRegularFrom()); // Assuming regular always starts from 0 if not specified otherwise
        assertEquals(v10, range.getRegularTo());
        assertEquals(v10, range.getHighFrom());
        assertEquals(v20, range.getHighTo());
        assertEquals(v20, range.getVeryHighFrom());
        assertEquals(v30, range.getVeryHighTo());
        assertEquals(v30, range.getExtremeFrom());
        assertEquals(Value.INFINITY, range.getExtremeTo());
    }

    @Test
    void testOfMethodUndefinedInputs() {
        assertEquals(BasicMetricsRange.UNDEFINED, BasicMetricsRange.of(vUndefined, v20, v30));
        assertEquals(BasicMetricsRange.UNDEFINED, BasicMetricsRange.of(v10, vUndefined, v30));
        assertEquals(BasicMetricsRange.UNDEFINED, BasicMetricsRange.of(v10, v20, vUndefined));
        assertEquals(BasicMetricsRange.UNDEFINED, BasicMetricsRange.of(vUndefined, vUndefined, vUndefined));
    }

    @Test
    void testOfMethodInvalidRangeOrder() {
        // regular > high
        assertEquals(BasicMetricsRange.UNDEFINED, BasicMetricsRange.of(v20, v10, v30));
        // high > veryHigh
        assertEquals(BasicMetricsRange.UNDEFINED, BasicMetricsRange.of(v10, v30, v20));
        // regular > veryHigh (implies regular > high and high > veryHigh if high is in between)
        assertEquals(BasicMetricsRange.UNDEFINED, BasicMetricsRange.of(v30, v20, v10));
    }

    // 2. `getRangeType` method
    @Test
    void testGetRangeType() {
        BasicMetricsRange range = BasicMetricsRange.of(v10, v20, v30); // Regular: [0-10), High: [10-20), VeryHigh: [20-30), Extreme: [30-inf)

        assertEquals(RangeType.UNDEFINED, range.getRangeType(vUndefined), "Undefined value should be RangeType.UNDEFINED");

        // REGULAR
        assertEquals(RangeType.REGULAR, range.getRangeType(v0), "Value 0 should be REGULAR");
        assertEquals(RangeType.REGULAR, range.getRangeType(Value.of(5)), "Value 5 should be REGULAR");
        assertEquals(RangeType.REGULAR, range.getRangeType(Value.of(9.99)), "Value 9.99 should be REGULAR");


        // HIGH (boundary value v10 is start of HIGH)
        assertEquals(RangeType.HIGH, range.getRangeType(v10), "Value 10 (boundary) should be HIGH");
        assertEquals(RangeType.HIGH, range.getRangeType(v15), "Value 15 should be HIGH");
        assertEquals(RangeType.HIGH, range.getRangeType(Value.of(19.99)), "Value 19.99 should be HIGH");

        // VERY_HIGH (boundary value v20 is start of VERY_HIGH)
        assertEquals(RangeType.VERY_HIGH, range.getRangeType(v20), "Value 20 (boundary) should be VERY_HIGH");
        assertEquals(RangeType.VERY_HIGH, range.getRangeType(v25), "Value 25 should be VERY_HIGH");
        assertEquals(RangeType.VERY_HIGH, range.getRangeType(Value.of(29.99)), "Value 29.99 should be VERY_HIGH");

        // EXTREME (boundary value v30 is start of EXTREME)
        assertEquals(RangeType.EXTREME, range.getRangeType(v30), "Value 30 (boundary) should be EXTREME");
        assertEquals(RangeType.EXTREME, range.getRangeType(v35), "Value 35 should be EXTREME");
        assertEquals(RangeType.EXTREME, range.getRangeType(Value.of(1000)), "Value 1000 should be EXTREME");

        // Test with negative values if applicable by range definition (assuming ranges are non-negative based on typical metrics)
        // If BasicMetricsRange can be defined with negative values, those tests would be here.
        // For now, assuming positive ranges starting from 0 for 'regular'.
        assertEquals(RangeType.REGULAR, range.getRangeType(vMinus5), "Value -5, assuming it falls to REGULAR or a specific handling");

        // Test with BasicMetricsRange.UNDEFINED
        assertEquals(RangeType.UNDEFINED, BasicMetricsRange.UNDEFINED.getRangeType(v10));
    }

    // 3. Getter methods
    @Test
    void testGetterMethods() {
        BasicMetricsRange range = BasicMetricsRange.of(v10, v20, v30);
        assertEquals(Value.of(0), range.getRegularFrom(), "getRegularFrom should be 0"); // Default assumption
        assertEquals(v10, range.getRegularTo(), "getRegularTo");
        assertEquals(v10, range.getHighFrom(), "getHighFrom");
        assertEquals(v20, range.getHighTo(), "getHighTo");
        assertEquals(v20, range.getVeryHighFrom(), "getVeryHighFrom");
        assertEquals(v30, range.getVeryHighTo(), "getVeryHighTo");
        assertEquals(v30, range.getExtremeFrom(), "getExtremeFrom");
        assertEquals(Value.INFINITY, range.getExtremeTo(), "getExtremeTo should be INFINITY");

        // Test getters for BasicMetricsRange.UNDEFINED
        assertEquals(vUndefined, BasicMetricsRange.UNDEFINED.getRegularFrom());
        assertEquals(vUndefined, BasicMetricsRange.UNDEFINED.getRegularTo());
        assertEquals(vUndefined, BasicMetricsRange.UNDEFINED.getHighFrom());
        // ... and so on for all getters of UNDEFINED range
    }

    // 4. `toString` and `percentageFormat` methods
    @Test
    void testToStringAndPercentageFormat() {
        BasicMetricsRange range = BasicMetricsRange.of(Value.of(0.1), Value.of(0.5), Value.of(0.8));
        // Example: Regular: [0.00-0.10), High: [0.10-0.50), VeryHigh: [0.50-0.80), Extreme: [0.80-inf)
        String expectedString = "[0.00-0.10), [0.10-0.50), [0.50-0.80), [0.80-∞)";
        assertEquals(expectedString, range.toString());

        String expectedPercentageString = "[0.00%-10.00%), [10.00%-50.00%), [50.00%-80.00%), [80.00%-∞)";
        assertEquals(expectedPercentageString, range.percentageFormat());

        assertEquals("", BasicMetricsRange.UNDEFINED.toString(), "toString for UNDEFINED range should be empty");
        assertEquals("", BasicMetricsRange.UNDEFINED.percentageFormat(), "percentageFormat for UNDEFINED range should be empty");
    }

    // 5. `getRangeByRangeType` method
    @Test
    void testGetRangeByRangeType() {
        BasicMetricsRange range = BasicMetricsRange.of(v10, v20, v30);

        assertEquals("[0-10)", range.getRangeByRangeType(RangeType.REGULAR));
        assertEquals("[10-20)", range.getRangeByRangeType(RangeType.HIGH));
        assertEquals("[20-30)", range.getRangeByRangeType(RangeType.VERY_HIGH));
        assertEquals("[30-∞)", range.getRangeByRangeType(RangeType.EXTREME));
        assertEquals("", range.getRangeByRangeType(RangeType.UNDEFINED));

        // Test with BasicMetricsRange.UNDEFINED
        assertEquals("", BasicMetricsRange.UNDEFINED.getRangeByRangeType(RangeType.REGULAR));
        assertEquals("", BasicMetricsRange.UNDEFINED.getRangeByRangeType(RangeType.UNDEFINED));

        // Test percentage formatting within getRangeByRangeType if it's implied by class design
        // For now, assuming it uses Value.toString() not percentageFormat()
        BasicMetricsRange percentRange = BasicMetricsRange.of(Value.of(0.1), Value.of(0.5), Value.of(0.8));
        assertEquals("[0.00-0.10)", percentRange.getRangeByRangeType(RangeType.REGULAR)); // Uses Value.toString by default
    }
}
