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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Objects;

public class Value extends Number implements Comparable<Value> {

    private final Number value;

    public static final Value UNDEFINED = new Value(0L) {
        @Override
        public String toString() {
            return "N/A";
        }
    };
    public static final Value ZERO = new Value(0L);
    public static final Value ONE = new Value(1L);
    public static final Value INFINITY = new Value(0L) {
        @Override
        public String toString() {
            return "Infinity";
        }

        @Override
        public boolean isGreaterThan(Value other) {
            return true;
        }

        @Override
        public boolean isEqualsOrGreaterThan(Value other) {
            return false;
        }

        @Override
        public boolean isLessThan(Value other) {
            return false;
        }

        @Override
        public boolean isEqualsOrLessThan(Value other) {
            return false;
        }
    };

    private static final DecimalFormat METRIC_VALUE_FORMAT = new DecimalFormat("0.0###");

    private Value(@NotNull Long value) {
        this.value = value;
    }

    private Value(@NotNull Double value) {
        this.value = value;
    }

    public static Value of(long l) {
        return new Value(l);
    }

    public static Value of(double d) {
        return new Value(d);
    }

//    public static Value of(@NotNull Number n) {
//        return new Value(n);
//    }

    public Value plus(@NotNull Value that) {
        Number other = that.value;
        if (value instanceof Long) {
            if (other instanceof Long) {
                return new Value(value.longValue() + other.longValue());
            }
            if (other instanceof Double) {
                return new Value(value.doubleValue() + other.doubleValue());
            }
        }
        if (value instanceof Double) {
            return new Value(value.doubleValue() + other.doubleValue());
        }
        return UNDEFINED;
    }

    public Value negate() {
        if (value instanceof Long) {
            return new Value(-value.longValue());
        }
        if (value instanceof Double) {
            return new Value(-value.doubleValue());
        }
        return UNDEFINED;
    }

    public Value minus(@NotNull Value that) {
        return this.plus(that.negate());
    }

    public Value times(@NotNull Value that) {
        Number other = that.value;
        if (value instanceof Long) {
            if (other instanceof Long) {
                return new Value(value.longValue() * other.longValue());
            }
            if (other instanceof Double) {
                return new Value(value.doubleValue() * other.doubleValue());
            }
        }
        if (value instanceof Double) {
            return new Value(value.doubleValue() * other.doubleValue());
        }
        return UNDEFINED;
    }

    public Value divide(@NotNull Value that) {
        Number other = that.value;
        if (value instanceof Long) {
            if (other instanceof Long) {
                if (other.longValue() == 0L) {
                    return UNDEFINED;
                }
                return new Value(value.longValue() / other.longValue());
            }
            if (other instanceof Double) {
                if (other.doubleValue() == 0.0) {
                    return UNDEFINED;
                }
                return new Value(value.doubleValue() / other.doubleValue());
            }
        }
        if (value instanceof Double) {
            if (other.doubleValue() == 0.0) {
                return UNDEFINED;
            }
            return new Value(value.doubleValue() / other.doubleValue());
        }
        return UNDEFINED;
    }

    public Value pow(int exp) {
        if (value instanceof Long) {
            return new Value(Math.pow(value.longValue(), exp));
        }
        if (value instanceof Double) {
            return new Value(Math.pow(value.doubleValue(), exp));
        }
        return UNDEFINED;
    }

    public Value abs() {
        if (this.isLessThan(Value.ZERO)) {
            return this.negate();
        } else {
            return this;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value v = (Value) o;
        Number other = ((Value) o).value;
        if (this.value instanceof Long) {
           if (other instanceof Long) {
               return Objects.equals(this.value.longValue(), other.longValue());
           }
           if (other instanceof Double) {
               return Objects.equals(this.value.doubleValue(), other.doubleValue());
           }
        }
        if (this.value instanceof Double) {
            return Objects.equals(this.value.doubleValue(), other.doubleValue());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        if (value instanceof Long) {
            return value.toString();
        } else {
            return METRIC_VALUE_FORMAT.format(value.doubleValue());
        }
    }

    @Override
    public int compareTo(@NotNull Value that) {
        Number other = that.value;
        if (value instanceof Long) {
            if (other instanceof Long) {
                return ((Long) value).compareTo((Long) other);
            }
            if (other instanceof Double) {
                return (Double.valueOf(value.longValue()).compareTo((Double) other));
            }
        }
        if (value instanceof Double) {
            return ((Double) value).compareTo(other.doubleValue());
        }
        throw new UnsupportedOperationException("Unable to compare: this = " + value + " that = " + other);
    }

    public boolean isGreaterThan(Value other) {
        if (other == Value.INFINITY) {
            return false;
        }
        return this.compareTo(other) > 0;
    }

    public boolean isEqualsOrGreaterThan(Value other) {
        if (other == Value.INFINITY) {
            return false;
        }
        return this.compareTo(other) >= 0;
    }

    public boolean isLessThan(Value other) {
        if (other == Value.INFINITY) {
            return true;
        }
        return this.compareTo(other) < 0;
    }

    public boolean isEqualsOrLessThan(Value other) {
        if (other == Value.INFINITY) {
            return false;
        }
        return this.compareTo(other) <= 0;
    }

    public String percentageFormat() {
        NumberFormat format = NumberFormat.getPercentInstance();
        format.setMinimumFractionDigits(4);
        return format.format(value);
    }

    public double doubleValue() {
        if (value instanceof Long) {
            return (double) value.longValue();
        }
        return value.doubleValue();
    }

    @Override
    public int intValue() {
        return 0;
    }

    public long longValue() {
        return value.longValue();
    }
    @Override
    public float floatValue() {
        return 0;
    }
}

