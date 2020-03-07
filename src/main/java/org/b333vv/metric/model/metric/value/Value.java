/*
 * Copyright 2020 b333vv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.b333vv.metric.model.metric.value;

import org.jetbrains.annotations.NotNull;
import org.jscience.mathematics.number.LargeInteger;
import org.jscience.mathematics.number.Number;
import org.jscience.mathematics.number.Rational;
import org.jscience.mathematics.number.Real;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Objects;

public class Value implements Comparable<Value> {
    public static final Value UNDEFINED = new Value(LargeInteger.ZERO) {
        @Override
        public String toString() {
            return "N/A";
        }
    };
    private static final DecimalFormat METRIC_VALUE_FORMAT = new DecimalFormat("0.0########");
    private Number value;

    private Value(Number value) {
        assert (value != null);
        this.value = value;
    }

    public static Value of(long l) {
        return new Value(LargeInteger.valueOf(l));
    }

    public static Value of(double d) {
        return new Value(Real.valueOf(d));
    }

    public static Value of(BigInteger value) {
        return new Value(LargeInteger.valueOf(value));
    }

    public static Value of(@NotNull Number n) {
        return new Value(n);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value v = (Value) o;
        if (this.value instanceof Real && v.value instanceof Real) {
            return ((Real) this.value).approximates((Real) v.value);
        } else {
            return Objects.equals(this.value, v.value);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        if (value instanceof LargeInteger) {
            return value.toString();
        } else {
            return METRIC_VALUE_FORMAT.format(value.doubleValue());
        }
    }

    @Override
    public int compareTo(@NotNull Value that) {
        Number other = that.value;
        if (value instanceof LargeInteger) {
            if (other instanceof LargeInteger) {
                return value.compareTo(other);
            } else if (other instanceof Rational) {
                return Converter.toRational((LargeInteger) value).compareTo((Rational) other);
            } else if (other instanceof Real) {
                return compareReals(Converter.toReal((LargeInteger) value), (Real) other);
            }

        } else if (value instanceof Rational) {
            if (other instanceof LargeInteger) {
                return 0-that.compareTo(this);
            } else if (other instanceof Rational) {
                return value.compareTo(other);
            } else if (other instanceof Real) {
                return compareReals(Converter.toReal((Rational) value), (Real) other);
            }

        } else if (value instanceof Real) {
            if (other instanceof LargeInteger) {
                return 0-that.compareTo(this);
            } else if (other instanceof Rational) {
                return 0-that.compareTo(this);
            } else if (other instanceof Real) {
                return compareReals((Real) this.value, (Real) other);
            }
        }

        throw new UnsupportedOperationException("Unable to compare " + value.getClass() + " to " + value.getClass());
    }

    private int compareReals(Real left, Real right) {
        if (left.approximates(right))
            return 0;
        else
            return left.compareTo(right);
    }

    public boolean isGreaterThan(Value other) {
        return this.compareTo(other) > 0;
    }

    public boolean isEqualsOrGreaterThan(Value other) {
        return this.compareTo(other) >= 0;
    }

    public boolean isLessThan(Value other) {
        return this.compareTo(other) < 0;
    }

    public boolean isEqualsOrLessThan(Value other) {
        return this.compareTo(other) <= 0;
    }

    public String percentageFormat() {
        NumberFormat format = NumberFormat.getPercentInstance();
        format.setMinimumFractionDigits(2);
        return format.format(value);
    }
}

