package org.b333vv.metric.model.metric.value;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ValueTest {

    // 1. Constructors and `of` methods
    @Test
    void testOfMethods() {
        Value longValue = Value.of(10L);
        assertEquals(10L, longValue.longValue());
        assertTrue(longValue.toString().equals("10"), "toString for Long value is incorrect");

        Value doubleValue = Value.of(10.5);
        assertEquals(10.5, doubleValue.doubleValue(), 0.001);
        assertTrue(doubleValue.toString().equals("10.50"), "toString for Double value is incorrect");

        // Check internal type if possible - assuming Value has a way to know or it's implicit by behavior
        // For now, relying on longValue() and doubleValue() behavior and toString()
    }

    // 2. Special values
    @Test
    void testSpecialValuesUndefinedZeroOne() {
        assertEquals("UNDEFINED", Value.UNDEFINED.toString(), "Value.UNDEFINED.toString() is incorrect");
        assertEquals(0L, Value.ZERO.longValue(), "Value.ZERO.longValue() is incorrect");
        assertEquals("0", Value.ZERO.toString(), "Value.ZERO.toString() is incorrect");
        assertEquals(1L, Value.ONE.longValue(), "Value.ONE.longValue() is incorrect");
        assertEquals("1", Value.ONE.toString(), "Value.ONE.toString() is incorrect");
    }

    @Test
    void testSpecialValueInfinity() {
        assertEquals("∞", Value.INFINITY.toString(), "Value.INFINITY.toString() is incorrect");

        Value regularValue = Value.of(100);
        assertTrue(Value.INFINITY.isGreaterThan(regularValue), "INFINITY should be greater than regular value");
        assertFalse(Value.INFINITY.isLessThan(regularValue), "INFINITY should not be less than regular value");
        assertTrue(regularValue.isLessThan(Value.INFINITY), "Regular value should be less than INFINITY");
        assertFalse(regularValue.isGreaterThan(Value.INFINITY), "Regular value should not be greater than INFINITY");
        assertEquals(0, Value.INFINITY.compareTo(Value.INFINITY), "INFINITY should be equal to INFINITY in comparison");
        assertTrue(Value.INFINITY.isEqualsOrGreaterThan(regularValue));
        assertTrue(Value.INFINITY.isEqualsOrGreaterThan(Value.INFINITY));
        assertFalse(Value.INFINITY.isEqualsOrLessThan(regularValue));
        assertTrue(Value.INFINITY.isEqualsOrLessThan(Value.INFINITY));
    }

    // 3. Arithmetic operations
    @Test
    void testArithmeticOperationsLong() {
        Value v10 = Value.of(10L);
        Value v5 = Value.of(5L);

        assertEquals(Value.of(15L), v10.plus(v5), "10L + 5L should be 15L");
        assertEquals(Value.of(5L), v10.minus(v5), "10L - 5L should be 5L");
        assertEquals(Value.of(50L), v10.times(v5), "10L * 5L should be 50L");
        assertEquals(Value.of(2L), v10.divide(v5), "10L / 5L should be 2L");
        assertEquals(Value.of(-10L), v10.negate(), "-10L should be -10L");
        assertEquals(Value.of(100L), v10.pow(2), "10L^2 should be 100L");
        assertEquals(Value.of(10L), v10.abs(), "abs(10L) should be 10L");
        assertEquals(Value.of(10L), Value.of(-10L).abs(), "abs(-10L) should be 10L");
    }

    @Test
    void testArithmeticOperationsDouble() {
        Value v10_5 = Value.of(10.5);
        Value v2_0 = Value.of(2.0);

        assertEquals(Value.of(12.5), v10_5.plus(v2_0), "10.5 + 2.0 should be 12.5");
        assertEquals(Value.of(8.5), v10_5.minus(v2_0), "10.5 - 2.0 should be 8.5");
        assertEquals(Value.of(21.0), v10_5.times(v2_0), "10.5 * 2.0 should be 21.0");
        assertEquals(Value.of(5.25), v10_5.divide(v2_0), "10.5 / 2.0 should be 5.25");
        assertEquals(Value.of(-10.5), v10_5.negate(), "-10.5 should be -10.5");
        assertEquals(Value.of(110.25), v10_5.pow(2), "10.5^2 should be 110.25"); // 10.5 * 10.5
        assertEquals(Value.of(10.5), v10_5.abs(), "abs(10.5) should be 10.5");
        assertEquals(Value.of(10.5), Value.of(-10.5).abs(), "abs(-10.5) should be 10.5");
    }

    @Test
    void testArithmeticOperationsMixed() {
        Value v10L = Value.of(10L);
        Value v2_5D = Value.of(2.5);

        // Long + Double -> Double
        assertEquals(Value.of(12.5), v10L.plus(v2_5D), "10L + 2.5D should be 12.5D");
        // Double + Long -> Double
        assertEquals(Value.of(12.5), v2_5D.plus(v10L), "2.5D + 10L should be 12.5D");

        // Long - Double -> Double
        assertEquals(Value.of(7.5), v10L.minus(v2_5D), "10L - 2.5D should be 7.5D");
        // Double - Long -> Double
        assertEquals(Value.of(-7.5), v2_5D.minus(v10L), "2.5D - 10L should be -7.5D");

        // Long * Double -> Double
        assertEquals(Value.of(25.0), v10L.times(v2_5D), "10L * 2.5D should be 25.0D");
        // Double * Long -> Double
        assertEquals(Value.of(25.0), v2_5D.times(v10L), "2.5D * 10L should be 25.0D");

        // Long / Double -> Double
        assertEquals(Value.of(4.0), v10L.divide(v2_5D), "10L / 2.5D should be 4.0D");
        // Double / Long -> Double
        assertEquals(Value.of(0.25), v2_5D.divide(v10L), "2.5D / 10L should be 0.25D");
    }

    @Test
    void testArithmeticDivisionByZero() {
        Value v10L = Value.of(10L);
        Value v10_5D = Value.of(10.5);
        Value vZero = Value.ZERO; // Value.of(0L) or Value.of(0.0)

        assertEquals(Value.UNDEFINED, v10L.divide(vZero), "10L / 0 should be UNDEFINED");
        assertEquals(Value.UNDEFINED, v10_5D.divide(vZero), "10.5D / 0 should be UNDEFINED");
        assertEquals(Value.UNDEFINED, v10L.divide(Value.of(0.0)), "10L / 0.0 should be UNDEFINED");
        assertEquals(Value.UNDEFINED, v10_5D.divide(Value.of(0L)), "10.5D / 0L should be UNDEFINED");
    }

    // 4. Comparison methods
    @Test
    void testComparisonMethods() {
        Value v10L = Value.of(10L);
        Value v10D = Value.of(10.0);
        Value v12L = Value.of(12L);
        Value v12D = Value.of(12.0);
        Value v8L = Value.of(8L);
        Value v8D = Value.of(8.0);

        // Equals (also tests compareTo indirectly)
        assertTrue(v10L.equals(v10D), "10L should be equal to 10.0D");
        assertTrue(v10D.equals(v10L), "10.0D should be equal to 10L");
        assertFalse(v10L.equals(v12L), "10L should not be equal to 12L");
        assertEquals(0, v10L.compareTo(v10D), "compareTo: 10L vs 10.0D");

        // isGreaterThan
        assertTrue(v12L.isGreaterThan(v10L), "12L > 10L");
        assertTrue(v12D.isGreaterThan(v10D), "12.0D > 10.0D");
        assertTrue(v12L.isGreaterThan(v10D), "12L > 10.0D");
        assertTrue(v12D.isGreaterThan(v10L), "12.0D > 10L");
        assertFalse(v10L.isGreaterThan(v12L), "10L not > 12L");

        // isEqualsOrGreaterThan
        assertTrue(v12L.isEqualsOrGreaterThan(v10L), "12L >= 10L");
        assertTrue(v10L.isEqualsOrGreaterThan(v10L), "10L >= 10L");
        assertTrue(v10L.isEqualsOrGreaterThan(v10D), "10L >= 10.0D");

        // isLessThan
        assertTrue(v8L.isLessThan(v10L), "8L < 10L");
        assertTrue(v8D.isLessThan(v10D), "8.0D < 10.0D");
        assertTrue(v8L.isLessThan(v10D), "8L < 10.0D");
        assertTrue(v8D.isLessThan(v10L), "8.0D < 10L");
        assertFalse(v10L.isLessThan(v8L), "10L not < 8L");

        // isEqualsOrLessThan
        assertTrue(v8L.isEqualsOrLessThan(v10L), "8L <= 10L");
        assertTrue(v10L.isEqualsOrLessThan(v10L), "10L <= 10L");
        assertTrue(v10L.isEqualsOrLessThan(v10D), "10L <= 10.0D");

        // CompareTo with different types
        assertTrue(v10L.compareTo(v12D) < 0, "10L compareTo 12.0D should be < 0");
        assertTrue(v12L.compareTo(v10D) > 0, "12L compareTo 10.0D should be > 0");
    }

    // 5. toString() and percentageFormat()
    @Test
    void testToStringAndPercentageFormat() {
        Value vLong = Value.of(123L);
        Value vDouble = Value.of(12.345);
        Value vDoubleRound = Value.of(12.3); // Will be 12.30
        Value vPercent = Value.of(0.756);

        assertEquals("123", vLong.toString(), "toString for 123L");
        assertEquals("12.35", vDouble.toString(), "toString for 12.345 (rounds to 2 decimal places)");
        assertEquals("12.30", vDoubleRound.toString(), "toString for 12.3 (should be 12.30)");


        assertEquals("75.60%", vPercent.percentageFormat(), "percentageFormat for 0.756");
        assertEquals("0.00%", Value.ZERO.percentageFormat(), "percentageFormat for ZERO");
        assertEquals("100.00%", Value.ONE.percentageFormat(), "percentageFormat for ONE");
        assertEquals("UNDEFINED", Value.UNDEFINED.percentageFormat(), "percentageFormat for UNDEFINED");
        assertEquals("∞", Value.INFINITY.percentageFormat(), "percentageFormat for INFINITY");

        // Test with a long value, should also work by converting to double
        assertEquals("50.00%", Value.of(0.5).percentageFormat(), "percentageFormat for Value.of(0.5D)");
    }

    // 6. doubleValue(), longValue()
    @Test
    void testTypeConversions() {
        Value vLong = Value.of(123L);
        Value vDouble = Value.of(12.789);
        Value vDoubleSmall = Value.of(0.123);
        Value vDoubleZeroPoint = Value.of(12.0);


        // doubleValue()
        assertEquals(123.0, vLong.doubleValue(), 0.001, "doubleValue for 123L");
        assertEquals(12.789, vDouble.doubleValue(), 0.001, "doubleValue for 12.789D");

        // longValue()
        assertEquals(123L, vLong.longValue(), "longValue for 123L");
        assertEquals(12L, vDouble.longValue(), "longValue for 12.789D (truncation expected)");
        assertEquals(0L, vDoubleSmall.longValue(), "longValue for 0.123D (truncation expected)");
        assertEquals(12L, vDoubleZeroPoint.longValue(), "longValue for 12.0D");


        // Behavior with special values
        assertEquals(0L, Value.ZERO.longValue());
        assertEquals(0.0, Value.ZERO.doubleValue(), 0.001);
        assertEquals(1L, Value.ONE.longValue());
        assertEquals(1.0, Value.ONE.doubleValue(), 0.001);

        // For UNDEFINED and INFINITY, longValue() and doubleValue() might throw or return specific values.
        // Based on current Value.java, it seems they will return 0 or throw an exception for UNDEFINED.
        // And for INFINITY, Double.POSITIVE_INFINITY for doubleValue, and a large long for longValue.
        // Let's assume they return 0 for UNDEFINED as a common case for "no value".
        // And for INFINITY, specific values.
        assertThrows(UnsupportedOperationException.class, () -> Value.UNDEFINED.longValue(), "longValue for UNDEFINED should throw");
        assertThrows(UnsupportedOperationException.class, () -> Value.UNDEFINED.doubleValue(), "doubleValue for UNDEFINED should throw");


        assertEquals(Double.POSITIVE_INFINITY, Value.INFINITY.doubleValue(), 0.001, "doubleValue for INFINITY");
        assertEquals(Long.MAX_VALUE, Value.INFINITY.longValue(), "longValue for INFINITY (expected Long.MAX_VALUE)");
    }
}
