package org.b333vv.metricsTree.model.metric.value;

public class Statistic {

    private Value total = null;
    private Value count = Value.ZERO;
    private Value max = null;
    private Value min = null;

    public Statistic() {

    }

    public Statistic(Value total, Value count, Value max, Value min) {
        this.total = total;
        this.count = count;
        this.max = max;
        this.min = min;
    }

    public static void accumulate(Statistic statistic, Value value) {
        statistic.add(value);
    }

    public static Statistic combine(Statistic left, Statistic right) {
        if (left.count == Value.ZERO) return right;
        if (right.count == Value.ZERO) return left;

        return new Statistic(
                left.total.plus(right.total),
                left.count.plus(right.count),
                Value.max(left.max, right.max),
                Value.min(left.min, right.min)
        );
    }

    public static Statistic finish(Statistic statistic) {
        return statistic;
    }

    public Value getSum() {
        return total != null ? total : Value.ZERO;
    }

    public Value getCount() {
        return count;
    }

    public Value getMax() {
        return max != null ? max : Value.ZERO;
    }

    public Value getMin() {
        return min != null ? min : Value.ZERO;
    }

    public Value getAverage() {
        return total != null ? total.divide(count) : Value.ZERO;
    }

    private void add(Value value) {

        if (total == null) {
            total = value;
        } else {
            total = total.plus(value);
        }

        if (max == null) {
            max = value;
        } else {
            max = Value.max(max, value);
        }

        if (min == null) {
            min = value;
        } else {
            min = Value.min(max, value);
        }

        count = count.plus(Value.ONE);

    }
}
