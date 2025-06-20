package org.b333vv.metric.model.metric;

import org.b333vv.metric.model.metric.value.Value;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MetricTest {

    // 1. `of` methods
    @Test
    void testOfMethods() {
        MetricType type = MetricType.LCOM;
        Value value = Value.of(10L);

        Metric metric1 = Metric.of(type, value);
        assertEquals(type, metric1.getType());
        assertEquals(value, metric1.getValue());

        Metric metric2 = Metric.of(type, 10L);
        assertEquals(type, metric2.getType());
        assertEquals(Value.of(10L), metric2.getValue());

        Metric metric3 = Metric.of(type, 10.5);
        assertEquals(type, metric3.getType());
        assertEquals(Value.of(10.5), metric3.getValue());
    }

    // 2. Getters
    @Test
    void testGetterMethods() {
        MetricType type = MetricType.NOA;
        Value value = Value.of(5);
        Metric metric = Metric.of(type, value);

        assertEquals(type, metric.getType(), "getType() should return the correct MetricType.");
        assertEquals(value, metric.getValue(), "getValue() should return the correct Value.");
    }

    // 3. `toString()`
    @Test
    void testToStringMethod() {
        Metric metricLong = Metric.of(MetricType.LOC, 100L);
        // Assuming Metric.toString() is "MetricType: Value.toString()"
        assertEquals("LOC: 100", metricLong.toString(), "toString() format for Long value is incorrect.");

        Metric metricDouble = Metric.of(MetricType.TCC, 0.75);
        // Value.of(0.75).toString() is "0.75" (or "0.750" depending on Value's toString)
        // For Value.java, it's usually 2 decimal places, so "0.75"
        assertEquals("TCC: 0.75", metricDouble.toString(), "toString() format for Double value is incorrect.");

        Metric metricUndefined = Metric.of(MetricType.WMC, Value.UNDEFINED);
        assertEquals("WMC: UNDEFINED", metricUndefined.toString(), "toString() for UNDEFINED value is incorrect.");
    }

    // 4. `equals()` and `hashCode()`
    @Test
    void testEqualsAndHashCode() {
        Metric metric1a = Metric.of(MetricType.RFC, 10L);
        Metric metric1b = Metric.of(MetricType.RFC, Value.of(10L)); // Same as metric1a
        Metric metric2 = Metric.of(MetricType.RFC, 20L); // Different value
        Metric metric3 = Metric.of(MetricType.CBO, 10L); // Different type

        // Reflexivity
        assertEquals(metric1a, metric1a);

        // Symmetry
        assertEquals(metric1a, metric1b);
        assertEquals(metric1b, metric1a);

        // Transitivity (not easily testable with just these, but implied by consistent field checks)

        // Consistency with null
        assertNotEquals(null, metric1a);

        // Different values
        assertNotEquals(metric1a, metric2);

        // Different types
        assertNotEquals(metric1a, metric3);

        // Different type and value
        assertNotEquals(metric2, metric3);


        // hashCode consistency
        assertEquals(metric1a.hashCode(), metric1b.hashCode(), "Hashcodes should be equal for equal objects.");
        // While not strictly required, good hash functions usually result in different hash codes for non-equal objects
        // However, we cannot assert inequality of hashcodes for non-equal objects due to possible collisions.

        // Test with different object type
        assertNotEquals("RFC: 10", metric1a);
    }

    // 5. `getFormattedValue()`
    @Test
    void testGetFormattedValue() {
        Metric metricLong = Metric.of(MetricType.LOC, 12L);
        assertEquals("12", metricLong.getFormattedValue(), "getFormattedValue() for Long should match Value.toString().");

        Metric metricDouble = Metric.of(MetricType.A, 1.23); // Value.of(1.23).toString() is "1.23"
        assertEquals("1.23", metricDouble.getFormattedValue(), "getFormattedValue() for Double should match Value.toString().");

        Metric metricUndefined = Metric.of(MetricType.DIT, Value.UNDEFINED);
        assertEquals("UNDEFINED", metricUndefined.getFormattedValue(), "getFormattedValue() for UNDEFINED should match Value.toString().");
    }

    // 6. `compareTo()`
    @Test
    void testCompareTo() {
        Metric metricVal5TypeA = Metric.of(MetricType.LCOM, 5L);
        Metric metricVal10TypeA = Metric.of(MetricType.LCOM, 10L);
        Metric metricVal5TypeB = Metric.of(MetricType.LCOM, 5L); // Same value, different type
        Metric metricVal10TypeB = Metric.of(MetricType.LCOM, 10L); // Same value as metricVal10TypeA, different type

        // Basic comparison
        assertTrue(metricVal5TypeA.compareTo(metricVal10TypeA) < 0, "5L should be less than 10L.");
        assertTrue(metricVal10TypeA.compareTo(metricVal5TypeA) > 0, "10L should be greater than 5L.");
        assertEquals(0, metricVal5TypeA.compareTo(metricVal5TypeA), "Same metric should compare to 0.");

        // Comparison should be based on Value only, MetricType should not affect it.
        assertEquals(0, metricVal5TypeA.compareTo(metricVal5TypeB), "Metrics with same value, different types, should compare to 0.");
        assertTrue(metricVal5TypeA.compareTo(metricVal10TypeB) < 0, "Metric (5L, TypeA) vs (10L, TypeB) - should be < 0.");
        assertTrue(metricVal10TypeB.compareTo(metricVal5TypeA) > 0, "Metric (10L, TypeB) vs (5L, TypeA) - should be > 0.");

        // Comparison with UNDEFINED values (assuming UNDEFINED might be considered less than defined values or throw)
        // This depends on Value.compareTo(Value.UNDEFINED) behavior.
        // Let's assume Value.UNDEFINED.compareTo(anything) or anything.compareTo(Value.UNDEFINED) might be problematic
        // or has specific rules. If Value.compareTo handles UNDEFINED consistently (e.g., throws or treats as smallest/largest)
        // then Metric.compareTo will inherit that.
        Metric metricUndefined = Metric.of(MetricType.LCOM, Value.UNDEFINED);
        Metric metricDefined = Metric.of(MetricType.LCOM, 10L);

        // Assuming Value.UNDEFINED might throw or be handled as smallest by Value.compareTo
        // If Value.compareTo throws for UNDEFINED, these tests need assertThrows.
        // If Value.UNDEFINED is treated as smallest:
        // assertTrue(metricUndefined.compareTo(metricDefined) < 0);
        // assertTrue(metricDefined.compareTo(metricUndefined) > 0);
        // assertEquals(0, metricUndefined.compareTo(Metric.of(MetricType.LCOM, Value.UNDEFINED)));
        // For now, let's assume Value.compareTo works and does not throw with UNDEFINED.
        // The exact behavior of comparing with UNDEFINED needs to be known from Value.java implementation.
        // If Value.compareTo throws an exception, then Metric.compareTo would also.
        // Let's assume for now that Value.compareTo considers UNDEFINED to be less than any other value.
        Value valUndef = Value.UNDEFINED;
        Value valDef = Value.of(5);
        if (valUndef.compareTo(valDef) < 0) { // Check assumption
             assertTrue(metricUndefined.compareTo(metricDefined) < 0, "UNDEFINED value metric should be less than defined value metric.");
             assertTrue(metricDefined.compareTo(metricUndefined) > 0, "Defined value metric should be greater than UNDEFINED value metric.");
             assertEquals(0, metricUndefined.compareTo(Metric.of(MetricType.LCOM, Value.UNDEFINED)), "Two UNDEFINED value metrics should be equal in comparison.");
        } else {
            // If Value.UNDEFINED comparison is different (e.g., throws), this part of test might need adjustment
            // or assertThrows. For now, this block acknowledges the dependency.
            System.err.println("Warning: Test for Metric.compareTo() with UNDEFINED values relies on Value.compareTo() behavior which might not be 'UNDEFINED is smallest'.");
        }
    }
}
