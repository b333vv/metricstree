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

package org.b333vv.metric.model.metric;

import org.b333vv.metric.model.metric.value.Range;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.util.MetricsService;

import java.util.Objects;

public class Metric {
    private final MetricType type;
    private final Value value;
    private final Range range;

    private Metric(MetricType type, Value value) {
        this.type = type;
        this.value = value;
        this.range = MetricsService.getRangeForMetric(type);
    }

    public static Metric of(MetricType type, Value value) {
        return new Metric(type, value);
    }

    public static Metric of(MetricType type, long value) {
        return new Metric(type, Value.of(value));
    }

    public static Metric of(MetricType type, double value) {
        return new Metric(type, Value.of(value));
    }

    public MetricType getType() {
        return type;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public String toString() {
        return type.name() + ": " + value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Metric)) return false;
        Metric metric = (Metric) o;
        return getType() == metric.getType() &&
                Objects.equals(getValue(), metric.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getValue());
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
}
