package org.b333vv.metric.model.metric.value;

import org.jscience.mathematics.number.Rational;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ValueTest {

    @Test
    public void max() {
    }

    @Test
    public void min() {
    }

    @Test
    public void plus() {
        Value integer = Value.of(5);
        Value rational = Value.of(Rational.valueOf(7, 3));
        Value real = Value.of(3.14159);

        assertEquals(integer.plus(integer), Value.of(10));
        assertEquals(integer.plus(rational), Value.of(Rational.valueOf(7, 3).plus(Rational.valueOf(5, 1))));
        assertEquals(integer.plus(real).toString(), "8,14159");
        assertEquals(rational.plus(integer), integer.plus(rational));
        assertEquals(rational.plus(rational), Value.of(Rational.valueOf(7, 3).plus(Rational.valueOf(7, 3))));
        assertEquals(rational.plus(real).toString(), "5,474923333");
        assertEquals(real.plus(integer), integer.plus(real));
        assertEquals(real.plus(rational), rational.plus(real));
        assertEquals(real.plus(real).toString(), "6,28318");
    }

    @Test
    public void negate() {
    }

    @Test
    public void minus() {
    }

    @Test
    public void times() {
    }

    @Test
    public void divide() {
    }

    @Test
    public void pow() {
    }

    @Test
    public void compareTo() {
    }

    @Test
    public void isGreaterThan() {
    }

    @Test
    public void isLessThan() {
    }

    @Test
    public void abs() {
    }
}