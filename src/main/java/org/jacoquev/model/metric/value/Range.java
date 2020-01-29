package org.jacoquev.model.metric.value;

public class Range {
    public static final Range UNDEFINED_RANGE = new Range(Value.of(0), Value.of(0)) {
        @Override
        public boolean includes(Value value) {
            return true;
        }

        @Override
        public String toString() {
            return "[undefined]";
        }
    };
    private Value from;
    private Value to;

    public Range(Value from, Value to) {
        this.from = from;
        this.to = to;
    }

    public static Range of(Value from, Value to) {
        return new Range(from, to);
    }

    public boolean includes(Value value) {
        return (value.isGreaterThan(from) || value.equals(from)) &&
                (value.isLessThan(to) || value.equals(to));
    }

    public Value getFrom() {
        return from;
    }

    public void setFrom(Value from) {
        this.from = from;
    }

    public Value getTo() {
        return to;
    }

    public void setTo(Value to) {
        this.to = to;
    }

    @Override
    public String toString() {
        return "[" +
                from +
                ".." +
                to +
                "]";
    }
}