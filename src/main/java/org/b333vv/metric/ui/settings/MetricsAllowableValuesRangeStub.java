package org.b333vv.metric.ui.settings;

import java.util.Objects;

public class MetricsAllowableValuesRangeStub {
    private String name;
    private String description;
    private String level;
    private boolean doubleValue;
    private double minDoubleValue;
    private double maxDoubleValue;
    private long minLongValue;
    private long maxLongValue;

    public MetricsAllowableValuesRangeStub(String name, String description, String level, boolean doubleValue,
                                           double minDoubleValue, double maxDoubleValue, long minLongValue, long maxLongValue) {
        this.name = name;
        this.description = description;
        this.level = level;
        this.doubleValue = doubleValue;
        this.minDoubleValue = minDoubleValue;
        this.maxDoubleValue = maxDoubleValue;
        this.minLongValue = minLongValue;
        this.maxLongValue = maxLongValue;
    }

    public MetricsAllowableValuesRangeStub(){}

    public boolean isDoubleValue() {
        return doubleValue;
    }

    public double getMinDoubleValue() {
        return minDoubleValue;
    }

    public void setMinDoubleValue(double minDoubleValue) {
        this.minDoubleValue = minDoubleValue;
    }

    public double getMaxDoubleValue() {
        return maxDoubleValue;
    }

    public void setMaxDoubleValue(double maxDoubleValue) {
        this.maxDoubleValue = maxDoubleValue;
    }

    public long getMinLongValue() {
        return minLongValue;
    }

    public void setMinLongValue(long minLongValue) {
        this.minLongValue = minLongValue;
    }

    public long getMaxLongValue() {
        return maxLongValue;
    }

    public void setMaxLongValue(long maxLongValue) {
        this.maxLongValue = maxLongValue;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDoubleValue(boolean doubleValue) {
        this.doubleValue = doubleValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetricsAllowableValuesRangeStub)) return false;
        MetricsAllowableValuesRangeStub that = (MetricsAllowableValuesRangeStub) o;
        return isDoubleValue() == that.isDoubleValue() &&
                Double.compare(that.getMinDoubleValue(), getMinDoubleValue()) == 0 &&
                Double.compare(that.getMaxDoubleValue(), getMaxDoubleValue()) == 0 &&
                getMinLongValue() == that.getMinLongValue() &&
                getMaxLongValue() == that.getMaxLongValue() &&
                getName().equals(that.getName()) &&
                getDescription().equals(that.getDescription()) &&
                getLevel().equals(that.getLevel());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getDescription(), getLevel(), isDoubleValue(),
                getMinDoubleValue(), getMaxDoubleValue(), getMinLongValue(), getMaxLongValue());
    }

    @Override
    public String toString() {
        return "[" + name + "] " + description;
    }
}
