package org.b333vv.metric.model.metric.value;

public class Range {
    public static final Range UNDEFINED = new Range(Value.UNDEFINED, Value.UNDEFINED) {
        @Override
        public boolean includes(Value value) {
            return true;
        }

        @Override
        public String toString() {
            return "";
        }

        @Override
        public String percentageFormat() {
            return "";
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
        return (value.isEqualsOrGreaterThan(from) && value.isEqualsOrLessThan(to));
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

    public String percentageFormat() {
        return "[" +
                from.percentageFormat() +
                ".." +
                to.percentageFormat() +
                "]";
    }
}
