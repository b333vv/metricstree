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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Objects;

public class Value implements Comparable<Value> {

    @SuppressWarnings("rawtypes")
    private final Number value;

    public static final Value UNDEFINED = new Value(LargeInteger.ZERO) {
        @Override
        public String toString() {
            return "N/A";
        }
    };
    public static final Value ZERO = new Value(LargeInteger.ZERO);
    public static final Value ONE = new Value(LargeInteger.ONE);
    public static final Value INFINITY = new Value(LargeInteger.ZERO) {
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

    @SuppressWarnings("rawtypes")
    private Value(@NotNull Number value) {
        this.value = value;
    }

    public static Value of(long l) {
        return new Value(LargeInteger.valueOf(l));
    }

    public static Value of(double d) {
        return new Value(Real.valueOf(d));
    }

    public static Value of(@NotNull BigInteger value) {
        return new Value(LargeInteger.valueOf(value));
    }

    @SuppressWarnings("rawtypes")
    public static Value of(@NotNull Number n) {
        return new Value(n);
    }

    public Value plus(@NotNull Value that) {
        @SuppressWarnings("rawtypes")
        Number other = that.value;
        if (value instanceof LargeInteger) {
            if (other instanceof LargeInteger) {
                return new Value(((LargeInteger) value).plus((LargeInteger)other));
            } else if (other instanceof Rational) {
                return new Value(((Rational)other).plus(Converter.toRational((LargeInteger) value)));
            } else if (other instanceof Real) {
                return new Value(((Real)other).plus(Converter.toReal((LargeInteger) value)));
            }
        } else if (value instanceof Rational) {
            if (other instanceof LargeInteger) {
                return that.plus(this);
            } else if (other instanceof Rational) {
                return new Value(((Rational)other).plus((Rational) value));
            } else if (other instanceof Real) {
                return new Value(((Real)other).plus(Converter.toReal((Rational) value)));
            }
        } else if( value instanceof Real) {
            if (other instanceof LargeInteger) {
                return that.plus(this);
            } else if (other instanceof Real) {
                return new Value(((Real)other).plus((Real) value));
            } else if (other instanceof Rational) {
                return that.plus(this);
            }
        }
        throw new UnsupportedOperationException("Unable to add " + value.getClass() + " to " + value.getClass());
    }

    public Value negate() {
        if (value instanceof LargeInteger) {
            return new Value(LargeInteger.ZERO.minus((LargeInteger) value));
        } else if (value instanceof Rational) {
            return new Value(Rational.ZERO.minus((Rational) value));
        } else if(value instanceof Real) {
            return new Value(Real.ZERO.minus((Real) value));
        }
        throw new UnsupportedOperationException("Unable to negate " + value.getClass());
    }

    public Value minus(@NotNull Value that) {
        return this.plus(that.negate());
    }

    public Value times(@NotNull Value that) {
        @SuppressWarnings("rawtypes")
        Number other = that.value;
        if(value instanceof LargeInteger) {
            if (other instanceof LargeInteger) {
                return new Value(((LargeInteger) value).times((LargeInteger) other));
            } else if (other instanceof Rational) {
                return new Value(((Rational) other).times(Converter.toRational((LargeInteger) value)));
            } else if (other instanceof Real) {
                return new Value(((Real) other).times(Converter.toReal((LargeInteger) value)));
            }
        } else if (value instanceof Rational) {
            if (other instanceof LargeInteger) {
                return that.times(this);
            } else if (other instanceof Rational) {
                return new Value(((Rational)other).times((Rational) value));
            } else if (other instanceof Real) {
                return new Value(((Real)other).times(Converter.toReal((Rational)value)));
            }
        } else if (value instanceof Real) {
            if (other instanceof LargeInteger) {
                return that.times(this);
            } else if (other instanceof Real) {
                return new Value(((Real)other).times((Real) value));
            } else if (other instanceof Rational) {
                return that.times(this);
            }
        }
        throw new UnsupportedOperationException("Unable to multiply " + value.getClass() + " to " + value.getClass());
    }

    public Value divide(@NotNull Value that) {
        @SuppressWarnings("rawtypes")
        Number other = that.value;
        if (value instanceof LargeInteger) {
            if (other instanceof LargeInteger) {
                return new Value(Rational.valueOf((LargeInteger) value, (LargeInteger)other));
            } else if (other instanceof Rational) {
                return new Value(Converter.toRational((LargeInteger) value).divide((Rational)other));
            } else if (other instanceof Real) {
                return new Value(Converter.toReal((LargeInteger) value).divide((Real)other));
            }
        } else if (value instanceof Rational) {
            if (other instanceof LargeInteger) {
                return new Value(((Rational) value).divide(Converter.toRational((LargeInteger) other)));
            } else if (other instanceof Rational) {
                return new Value(((Rational) value).divide((Rational) other));
            } else if (other instanceof Real) {
                return new Value(Converter.toReal((Rational) value).divide((Real)other));
            }
        } else if (value instanceof Real) {
            if (other instanceof LargeInteger) {
                return new Value(((Real) value).divide(Converter.toReal((LargeInteger) other)));
            } else if (other instanceof Rational) {
                return new Value(((Real) value).divide(Converter.toReal((Rational) other)));
            } else if (other instanceof Real) {
                return new Value(((Real) value).divide((Real) other));
            }
        }
        throw new UnsupportedOperationException("Unable to divide " + value.getClass() + " to " + value.getClass());
    }

    public Value pow(int exp) {
        return new Value(value.pow(exp));
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
        @SuppressWarnings("rawtypes")
        Number other = that.value;
        if (value instanceof LargeInteger) {
            if (other instanceof LargeInteger) {
                @SuppressWarnings("unchecked")
                int i = value.compareTo(other);
                return i;
            } else if (other instanceof Rational) {
                return Converter.toRational((LargeInteger) value).compareTo((Rational) other);
            } else if (other instanceof Real) {
                return compareReals(Converter.toReal((LargeInteger) value), (Real) other);
            }
        } else if (value instanceof Rational) {
            if (other instanceof LargeInteger) {
                return -that.compareTo(this);
            } else if (other instanceof Rational) {
                @SuppressWarnings("unchecked")
                int i = value.compareTo(other);
                return i;
            } else if (other instanceof Real) {
                return compareReals(Converter.toReal((Rational) value), (Real) other);
            }
        } else if (value instanceof Real) {
            if (other instanceof LargeInteger) {
                return -that.compareTo(this);
            } else if (other instanceof Rational) {
                return -that.compareTo(this);
            } else if (other instanceof Real) {
                return compareReals((Real) this.value, (Real) other);
            }
        }
        throw new UnsupportedOperationException("Unable to compare " + value.getClass() + " to " + value.getClass());
    }

    private int compareReals(Real left, Real right) {
        if (left.approximates(right)) {
            return 0;
        }
        else {
            return left.compareTo(right);
        }
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
}

