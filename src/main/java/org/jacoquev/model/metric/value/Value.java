package org.jacoquev.model.metric.value;

import org.jscience.mathematics.number.LargeInteger;
import org.jscience.mathematics.number.Number;
import org.jscience.mathematics.number.Rational;
import org.jscience.mathematics.number.Real;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class Value implements Comparable<Value> {
    public static final Value ZERO = new Value(LargeInteger.ZERO);
    public static final Value ONE = new Value(LargeInteger.ONE);
    private static final DecimalFormat METRIC_VALUE_FORMAT = new DecimalFormat("0.0########");
    private Number value;

    private Value(Number value) {
        assert (value != null);
        this.value = value;
    }

    public static Value of(long l) {
        return new Value(LargeInteger.valueOf(l));
    }

    public static Value ofRational(long numerator, long denominator) {
        return new Value(Rational.valueOf(numerator, denominator));
    }

    public static Value of(double d) {
        return new Value(Real.valueOf(d));
    }

    public static Value of(BigInteger value) {
        return new Value(LargeInteger.valueOf(value));
    }

    public static Value of(Number n) {
        assert (n != null);
        return new Value(n);
    }

    public static Value max(Value left, Value right) {
        if (left.minus(right).compareTo(Value.ZERO) < 0) return right;
        else return left;
    }

    public static Value min(Value left, Value right) {
        if (left.minus(right).compareTo(Value.ZERO) < 0) return left;
        else return right;
    }

    public static Collector<? super Value, Statistic, Statistic> summarizingCollector() {
        return new Collector<Value, Statistic, Statistic>() {
            @Override
            public Supplier<Statistic> supplier() {
                return () -> new Statistic();
            }

            @Override
            public BiConsumer<Statistic, Value> accumulator() {
                return Statistic::accumulate;
            }

            @Override
            public BinaryOperator<Statistic> combiner() {
                return Statistic::combine;
            }

            @Override
            public Function<Statistic, Statistic> finisher() {
                return Statistic::finish;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH));
            }
        };
    }

    public Number getValue() {
        return value;
    }

    public Value plus(Value that) {
        assert (that != null);

        Number other = that.value;
        if (value instanceof LargeInteger) {
            if (other instanceof LargeInteger) {
                return new Value(((LargeInteger) value).plus((LargeInteger) other));
            } else if (other instanceof Rational) {
                return new Value(((Rational) other).plus(Converter.toRational((LargeInteger) value)));
            } else if (other instanceof Real) {
                return new Value(((Real) other).plus(Converter.toReal((LargeInteger) value)));
            }

        } else if (value instanceof Rational) {
            if (other instanceof LargeInteger) {
                return that.plus(this);
            } else if (other instanceof Rational) {
                return new Value(((Rational) other).plus((Rational) value));
            } else if (other instanceof Real) {
                return new Value(((Real) other).plus(Converter.toReal((Rational) value)));
            }

        } else if (value instanceof Real) {
            if (other instanceof LargeInteger) {
                return that.plus(this);
            } else if (other instanceof Real) {
                return new Value(((Real) other).plus((Real) value));
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
        } else if (value instanceof Real) {
            return new Value(Real.ZERO.minus((Real) value));
        }

        throw new UnsupportedOperationException("Unable to negate " + value.getClass());
    }

    public Value minus(Value that) {
        assert (that != null);
        return this.plus(that.negate());
    }

    public Value times(Value that) {
        assert (that != null);

        Number other = that.value;
        if (value instanceof LargeInteger) {
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
                return new Value(((Rational) other).times((Rational) value));
            } else if (other instanceof Real) {
                return new Value(((Real) other).times(Converter.toReal((Rational) value)));
            }

        } else if (value instanceof Real) {
            if (other instanceof LargeInteger) {
                return that.times(this);
            } else if (other instanceof Real) {
                return new Value(((Real) other).times((Real) value));
            } else if (other instanceof Rational) {
                return that.times(this);
            }
        }

        throw new UnsupportedOperationException("Unable to multiply " + value.getClass() + " to " + value.getClass());
    }

    public Value divide(Value that) {
        assert (that != null);

        Number other = that.value;
        if (value instanceof LargeInteger) {
            if (other instanceof LargeInteger) {
                return new Value(Rational.valueOf((LargeInteger) value, (LargeInteger) other));
            } else if (other instanceof Rational) {
                return new Value(Converter.toRational((LargeInteger) value).divide((Rational) other));
            } else if (other instanceof Real) {
                return new Value(Converter.toReal((LargeInteger) value).divide((Real) other));
            }

        } else if (value instanceof Rational) {
            if (other instanceof LargeInteger) {
                return new Value(((Rational) value).divide(Converter.toRational((LargeInteger) other)));
            } else if (other instanceof Rational) {
                return new Value(((Rational) value).divide((Rational) other));
            } else if (other instanceof Real) {
                return new Value(Converter.toReal((Rational) value).divide((Real) other));
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value value = (Value) o;
        if (this.value instanceof Real && value.value instanceof Real) {
            return ((Real) this.value).approximates((Real) value.value);
        } else {
            return Objects.equals(this.value, value.value);
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

    public long longValue() {
        return value.longValue();
    }

    public double doubleValue() {
        return value.doubleValue();
    }

    @Override
    public int compareTo(Value that) {
        assert (that != null);
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
                return 0 - that.compareTo(this);
            } else if (other instanceof Rational) {
                return value.compareTo(other);
            } else if (other instanceof Real) {
                return compareReals(Converter.toReal((Rational) value), (Real) other);
            }

        } else if (value instanceof Real) {
            if (other instanceof LargeInteger) {
                return 0 - that.compareTo(this);
            } else if (other instanceof Rational) {
                return 0 - that.compareTo(this);
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

    public boolean isLessThan(Value other) {
        return this.compareTo(other) < 0;
    }

    public Value abs() {
        if (this.isLessThan(Value.ZERO)) {
            return this.negate();
        } else {
            return this;
        }
    }
}

