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

package org.b333vv.metric.ui.settings.ranges;

import java.util.Objects;

public class DerivativeMetricsValidRangeStub {
    private String name;
    private String description;
    private String level;
    private double minValue;
    private double maxValue;

    public DerivativeMetricsValidRangeStub(String name, String description, String level,
                                           double minValue, double maxValue) {
        this.name = name;
        this.description = description;
        this.level = level;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public DerivativeMetricsValidRangeStub(){}

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DerivativeMetricsValidRangeStub)) return false;
        DerivativeMetricsValidRangeStub that = (DerivativeMetricsValidRangeStub) o;
        return Double.compare(that.getMinValue(), getMinValue()) == 0 &&
                Double.compare(that.getMaxValue(), getMaxValue()) == 0 &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getLevel(), that.getLevel());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getDescription(), getLevel(), getMinValue(), getMaxValue());
    }

    @Override
    public String toString() {
        return "[" + name + "] " + description;
    }
}
