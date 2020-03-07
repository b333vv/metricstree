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

import com.google.common.base.Objects;
import org.b333vv.metric.model.metric.value.Range;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.util.MetricsService;

public class Metric {
    private final String name;
    private final String description;
    private final Value value;
    private final Range range;
    private final String descriptionUrl;

    protected Metric(String name, String description, String descriptionUrl, Value value) {
        this.name = name;
        this.description = description;
        this.descriptionUrl = descriptionUrl;
        this.value = value;
        this.range = MetricsService.getRangeForMetric(name);
    }

    public static Metric of(String name, String description, String descriptionUrl, Value value) {
        return new Metric(name, description, descriptionUrl, value);
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
