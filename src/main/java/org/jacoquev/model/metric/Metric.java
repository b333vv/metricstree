package org.jacoquev.model.metric;

import com.google.common.base.Objects;
import org.jacoquev.model.metric.value.Range;
import org.jacoquev.model.metric.value.Value;
import org.jacoquev.util.MetricsService;

import java.text.NumberFormat;

public class Metric {
    private String name;
    private String description;
    private Value value;
    private Range range = Range.UNDEFINED;
    private String descriptionUrl;

    protected Metric(String name, String description, String descriptionUrl, Value value) {
        this.name = name;
        this.description = description;
        this.descriptionUrl = descriptionUrl;
        this.value = value;
        this.range = MetricsService.getRangeForMetric(name);
    }

    protected Metric(String name, String description, Value value) {
        this.name = name;
        this.description = description;
        this.value = value;
        this.range = MetricsService.getRangeForMetric(name);
    }

    protected Metric(String name, String description, Value from, Value to) {
        this.name = name;
        this.description = description;
        this.range = Range.of(from, to);
    }

    public static Metric of() {
        return new Metric("Metric name", "Metric description", Value.UNDEFINED);
    }

    public static Metric of(String name, String description, long from, long to) {
        return new Metric(name, description, Value.of(from), Value.of(to));
    }

    public static Metric of(String name, String description, double from, double to) {
        return new Metric(name, description, Value.of(from), Value.of(to));
    }

    public static Metric of(String name, String description, Value from, Value to) {
        return new Metric(name, description, from, to);
    }

    public static Metric of(String name, String description, Value value) {
        return new Metric(name, description, value);
    }

    public static Metric of(String name, String description, String descriptionUrl, Value value) {
        return new Metric(name, description, descriptionUrl, value);
    }

    public static Metric of(String name, String description, long value) {
        return new Metric(name, description, Value.of(value));
    }

    public static Metric of(String name, String description, double value) {
        return new Metric(name, description, Value.of(value));
    }

    public static Metric of(String name, String description, String descriptionUrl, long value) {
        return new Metric(name, description, descriptionUrl, Value.of(value));
    }

    public static Metric of(String name, String description, String descriptionUrl, double value) {
        return new Metric(name, description, descriptionUrl, Value.of(value));
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public String toString() {
        return name + ": " + value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Metric)) return false;
        Metric that = (Metric) o;
        return Objects.equal(name, that.name) &&
                Objects.equal(description, that.description) &&
                Objects.equal(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, description, value);
    }

    public String getFormattedValue() {
        return value.toString();
    }

    public Range getRange() {
        return range;
    }

    public boolean hasAllowableValue() {
        return range.includes(value);
    }

    public String getDescriptionUrl() {
        return descriptionUrl;
    }

}
